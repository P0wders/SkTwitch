package com.p0wders.sktwitch;

import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class TwitchClient extends WebSocketClient {

    private final String channelKey;
    private final String channelHash;
    private final @Nullable String oauthToken;
    private final String nick;
    private final boolean autoReconnect;
    private final boolean requestTags;
    private final boolean requestCommands;
    private final boolean requestMembership;
    private final BridgeManager manager;

    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private int reconnectAttempts = 0;

    /**
     * When membership is requested, we expect Twitch to send the NAMES list
     * (and subsequent JOINs) shortly after our own JOIN succeeds. If nothing
     * membership-related arrives in {@link #LARGE_CHANNEL_DETECT_TICKS} ticks,
     * the channel is almost certainly over Twitch's ~1000-viewer threshold,
     * where JOIN/PART/NAMES are suppressed entirely. We warn once and clear
     * the flag so we don't spam on reconnect cycles.
     */
    private volatile boolean membershipObserved = false;
    private volatile int largeChannelWarningTaskId = -1;
    private static final long LARGE_CHANNEL_DETECT_TICKS = 200L; // 10 seconds

    public TwitchClient(String channel, @Nullable String oauthToken, @Nullable String nickname,
                        boolean autoReconnect, boolean requestTags, boolean requestCommands,
                        boolean requestMembership, BridgeManager manager) {
        super(URI.create("wss://irc-ws.chat.twitch.tv:443"));
        this.channelKey = channel.toLowerCase().replace("#", "");
        this.channelHash = "#" + channelKey;
        this.oauthToken = oauthToken;
        this.nick = nickname != null ? nickname : "justinfan" + (10000 + new Random().nextInt(90000));
        this.autoReconnect = autoReconnect;
        this.requestTags = requestTags;
        this.requestCommands = requestCommands;
        this.requestMembership = requestMembership;
        this.manager = manager;
    }

    @Override
    public void onOpen(ServerHandshake h) {
        reconnectAttempts = 0;
        StringBuilder caps = new StringBuilder("CAP REQ :");
        if (requestTags) caps.append("twitch.tv/tags ");
        if (requestCommands) caps.append("twitch.tv/commands ");
        if (requestMembership) caps.append("twitch.tv/membership ");
        if (!caps.toString().equals("CAP REQ :")) {
            send(caps.toString().trim());
        }
        if (oauthToken != null) {
            send("PASS " + (oauthToken.startsWith("oauth:") ? oauthToken : "oauth:" + oauthToken));
        }
        send("NICK " + nick);
        send("JOIN " + channelHash);

        // Arm the large-channel warning timer only when membership was requested.
        // On a small channel, Twitch sends NAMES (353) within ~1-2 seconds of JOIN.
        // On a large channel (>~1000 viewers), Twitch never sends anything membership-related.
        if (requestMembership && largeChannelWarningTaskId == -1) {
            membershipObserved = false;
            try {
                largeChannelWarningTaskId = Bukkit.getScheduler().runTaskLater(
                        SkTwitch.getInstance(),
                        this::checkLargeChannelSilence,
                        LARGE_CHANNEL_DETECT_TICKS
                ).getTaskId();
            } catch (IllegalStateException ignored) {
                // plugin is shutting down — skip
            }
        }
    }

    private void checkLargeChannelSilence() {
        largeChannelWarningTaskId = -1;
        if (membershipObserved || shutdownRequested.get()) return;
        SkTwitch.getInstance().log("&e⚠ &d#" + channelKey + "&e has more than ~1000 viewers; "
                + "Twitch suppresses &fJOIN&e/&fPART&e/&fNAMES&e on large channels. "
                + "&7Membership events will not fire for this channel until it drops below the threshold.");
    }

    @Override
    public void onMessage(String raw) {
        for (String line : raw.split("\r\n")) {
            if (line.startsWith("PING")) {
                send("PONG :tmi.twitch.tv");
                continue;
            }

            String tagsPart = "";
            String rest = line;
            if (line.startsWith("@")) {
                int sp = line.indexOf(' ');
                tagsPart = line.substring(1, sp);
                rest = line.substring(sp + 1);
            }

            if (rest.contains(" USERNOTICE ")) {
                handleUserNotice(tagsPart, rest);
                continue;
            }
            if (rest.contains(" CLEARCHAT ")) {
                handleClearChat(tagsPart, rest);
                continue;
            }
            if (rest.contains(" CLEARMSG ")) {
                handleClearMsg(tagsPart, rest);
                continue;
            }
            if (rest.contains(" NOTICE ")) {
                handleNotice(tagsPart, rest);
                continue;
            }
            if (rest.contains(" ROOMSTATE ")) {
                handleRoomState(tagsPart, rest);
                continue;
            }
            if (rest.contains(" JOIN ")) {
                // Only a JOIN from someone other than us counts as evidence
                // that Twitch is sending us membership traffic for real users.
                String loginPeek = extractPrefixLogin(rest);
                if (loginPeek != null && !loginPeek.equalsIgnoreCase(nick)) {
                    membershipObserved = true;
                }
                handleJoin(rest);
                continue;
            }
            if (rest.contains(" PART ")) {
                String loginPeek = extractPrefixLogin(rest);
                if (loginPeek != null && !loginPeek.equalsIgnoreCase(nick)) {
                    membershipObserved = true;
                }
                handlePart(rest);
                continue;
            }
            // 353 = NAMES reply, 366 = end of NAMES. Even an empty NAMES is
            // a real membership response from Twitch — small channels get one,
            // large ones don't — so receiving either confirms it works here.
            if (rest.contains(" 353 ") || rest.contains(" 366 ")) {
                membershipObserved = true;
                continue;
            }
            if (rest.contains(" HOSTTARGET ")) {
                handleHostTarget(rest);
                continue;
            }
            if (rest.startsWith("RECONNECT") || rest.contains(" RECONNECT")) {
                handleReconnect();
                continue;
            }
            // USERSTATE and GLOBALUSERSTATE: log only — they describe the bot's
            // own identity, which on justinfan is empty. Useful diagnostic for
            // OAuth users later but no script-visible event today.
            if (rest.contains(" USERSTATE ") || rest.contains(" GLOBALUSERSTATE")) {
                continue;
            }
            if (!rest.contains(" PRIVMSG ")) continue;

            String user = rest.split("!", 2)[0].substring(1);
            String[] split = rest.split("PRIVMSG", 2);
            if (split.length < 2) continue;
            String[] msgSplit = split[1].split(":", 2);
            if (msgSplit.length < 2) continue;

            String emotesTag = parseTag(tagsPart, "emotes", "");
            String message = wrapTwitchEmotes(msgSplit[1], emotesTag);
            message = sanitizeMessage(message);

            String displayName = parseTag(tagsPart, "display-name", user);
            String color = parseTag(tagsPart, "color", "");
            String userId = parseTag(tagsPart, "user-id", "");
            String messageId = parseTag(tagsPart, "id", "");
            String roomId = parseTag(tagsPart, "room-id", "");
            String badges = parseTag(tagsPart, "badges", "");
            String badgeInfo = parseTag(tagsPart, "badge-info", "");
            String userType = parseTag(tagsPart, "user-type", "");

            boolean isFounder = badges.contains("founder/");
            boolean isSubscriber = badges.contains("subscriber/") || isFounder;
            boolean isModerator = badges.contains("moderator/") || "1".equals(parseTag(tagsPart, "mod", "0"));
            boolean isBroadcaster = badges.contains("broadcaster/");
            boolean isVip = badges.contains("vip/") || !parseTag(tagsPart, "vip", "").isEmpty();
            boolean isTurbo = badges.contains("turbo/");
            boolean isFirstMessage = "1".equals(parseTag(tagsPart, "first-msg", "0"));
            boolean isReturningChatter = "1".equals(parseTag(tagsPart, "returning-chatter", "0"));

            int subMonths = 0;
            if (isSubscriber) {
                for (String badge : badges.split(",")) {
                    if (badge.startsWith("subscriber/") || badge.startsWith("founder/")) {
                        try {
                            subMonths = Integer.parseInt(badge.substring(badge.indexOf('/') + 1));
                        } catch (NumberFormatException ignored) {}
                        break;
                    }
                }
            }

            int exactSubMonths = 0;
            if (!badgeInfo.isEmpty()) {
                for (String info : badgeInfo.split(",")) {
                    if (info.startsWith("subscriber/") || info.startsWith("founder/")) {
                        try {
                            exactSubMonths = Integer.parseInt(info.substring(info.indexOf('/') + 1));
                        } catch (NumberFormatException ignored) {}
                        break;
                    }
                }
            }

            String replyParentMsgId = parseTag(tagsPart, "reply-parent-msg-id", "");
            String replyParentUserId = parseTag(tagsPart, "reply-parent-user-id", "");
            String replyParentLogin = parseTag(tagsPart, "reply-parent-user-login", "");
            String replyParentDisplayName = parseTag(tagsPart, "reply-parent-display-name", "");
            String replyParentBody = parseTag(tagsPart, "reply-parent-msg-body", "");

            boolean isReply = !replyParentMsgId.isEmpty();
            TwitchUser replyParentUser = null;
            String replyParentMessage = "";
            if (isReply) {
                replyParentUser = stubUser(replyParentLogin, replyParentDisplayName, replyParentUserId);
                replyParentMessage = sanitizeMessage(unescapeIrcTag(replyParentBody));
            }

            String threadParentMsgId = parseTag(tagsPart, "reply-thread-parent-msg-id", "");
            String threadParentLogin = parseTag(tagsPart, "reply-thread-parent-user-login", "");
            String threadParentDisplay = parseTag(tagsPart, "reply-thread-parent-display-name", "");
            String threadParentUserId = parseTag(tagsPart, "reply-thread-parent-user-id", "");
            TwitchUser threadParentUser = threadParentMsgId.isEmpty() ? null
                    : stubUser(threadParentLogin, threadParentDisplay, threadParentUserId);

            long timestamp = parseLongTag(tagsPart, "tmi-sent-ts", 0L);
            String customRewardId = parseTag(tagsPart, "custom-reward-id", "");
            String flags = parseTag(tagsPart, "flags", "");

            int hypeAmount = parseIntTag(tagsPart, "pinned-chat-paid-amount", 0);
            String hypeCurrency = parseTag(tagsPart, "pinned-chat-paid-currency", "");
            int hypeLevel = 0;
            String hypeLevelTag = parseTag(tagsPart, "pinned-chat-paid-level", "");
            if (!hypeLevelTag.isEmpty()) {
                hypeLevel = parseHypeLevel(hypeLevelTag);
            }
            boolean hypeSystem = "1".equals(parseTag(tagsPart, "pinned-chat-paid-is-system-message", "0"));

            String sourceRoomId = parseTag(tagsPart, "source-room-id", "");
            String sourceMsgId = parseTag(tagsPart, "source-id", "");
            if (!sourceRoomId.isEmpty() && sourceRoomId.equals(roomId)) {
                sourceRoomId = "";
                sourceMsgId = "";
            }

            TwitchUser twitchUser = new TwitchUser(user, displayName, userId, color, badges,
                    badgeInfo, userType,
                    isSubscriber, isModerator, isBroadcaster, isVip, isTurbo, isFounder,
                    subMonths, exactSubMonths);
            TwitchChannel twitchChannel = new TwitchChannel(channelHash, roomId);

            manager.cacheUser(twitchChannel, twitchUser);
            manager.cacheChannel(twitchChannel);

            int bits = parseIntTag(tagsPart, "bits", 0);
            if (bits > 0) {
                manager.dispatchCheer(twitchChannel, twitchUser, bits, message);
            }

            manager.dispatch(twitchChannel, twitchUser, message, messageId, isFirstMessage,
                    isReturningChatter,
                    isReply, replyParentUser, replyParentMessage, replyParentMsgId,
                    threadParentUser, threadParentMsgId,
                    timestamp, customRewardId, flags,
                    hypeAmount, hypeCurrency, hypeLevel, hypeSystem,
                    sourceRoomId, sourceMsgId);
        }
    }

    private void handleUserNotice(String tagsPart, String rest) {
        String msgId = parseTag(tagsPart, "msg-id", "");
        String roomId = parseTag(tagsPart, "room-id", "");
        TwitchChannel ch = new TwitchChannel(channelHash, roomId);

        switch (msgId) {
            case "sub":
            case "resub": {
                TwitchUser user = userFromTags(tagsPart);
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                String planName = unescapeIrcTag(parseTag(tagsPart, "msg-param-sub-plan-name", ""));
                int cumulative = parseIntTag(tagsPart, "msg-param-cumulative-months", 1);
                int streak = "1".equals(parseTag(tagsPart, "msg-param-should-share-streak", "0"))
                        ? parseIntTag(tagsPart, "msg-param-streak-months", 0) : 0;
                int multiDur = parseIntTag(tagsPart, "msg-param-multimonth-duration", 0);
                int multiTen = parseIntTag(tagsPart, "msg-param-multimonth-tenure", 0);
                String body = null;
                int colon = rest.indexOf(" :");
                if (colon > -1 && colon + 2 < rest.length()) {
                    body = sanitizeMessage(rest.substring(colon + 2));
                }
                manager.dispatchSubscribe(ch, user, tier, planName,
                        cumulative, streak, multiDur, multiTen, "resub".equals(msgId), body);
                break;
            }
            case "subgift": {
                TwitchUser gifter = userFromTags(tagsPart);
                TwitchUser recipient = stubUser(
                        parseTag(tagsPart, "msg-param-recipient-user-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-display-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-id", ""));
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                String planName = unescapeIrcTag(parseTag(tagsPart, "msg-param-sub-plan-name", ""));
                int recipientTotalMonths = parseIntTag(tagsPart, "msg-param-months", 0);
                int giftDuration = parseIntTag(tagsPart, "msg-param-gift-months", 1);
                String originId = parseTag(tagsPart, "msg-param-origin-id", "");
                int senderTotal = parseIntTag(tagsPart, "msg-param-sender-count", 0);
                manager.dispatchGiftSub(ch, gifter, recipient, tier, planName,
                        recipientTotalMonths, giftDuration, originId, senderTotal);
                break;
            }
            case "anonsubgift": {
                TwitchUser recipient = stubUser(
                        parseTag(tagsPart, "msg-param-recipient-user-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-display-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-id", ""));
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                String planName = unescapeIrcTag(parseTag(tagsPart, "msg-param-sub-plan-name", ""));
                int recipientTotalMonths = parseIntTag(tagsPart, "msg-param-months", 0);
                int giftDuration = parseIntTag(tagsPart, "msg-param-gift-months", 1);
                String originId = parseTag(tagsPart, "msg-param-origin-id", "");
                manager.dispatchGiftSub(ch, null, recipient, tier, planName,
                        recipientTotalMonths, giftDuration, originId, 0);
                break;
            }
            case "submysterygift": {
                TwitchUser gifter = userFromTags(tagsPart);
                int count = parseIntTag(tagsPart, "msg-param-mass-gift-count", 1);
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                String planName = unescapeIrcTag(parseTag(tagsPart, "msg-param-sub-plan-name", ""));
                String originId = parseTag(tagsPart, "msg-param-origin-id", "");
                int senderTotal = parseIntTag(tagsPart, "msg-param-sender-count", 0);
                manager.dispatchMassGiftSub(ch, gifter, count, tier, planName, originId, senderTotal);
                break;
            }
            case "anonsubmysterygift": {
                int count = parseIntTag(tagsPart, "msg-param-mass-gift-count", 1);
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                String planName = unescapeIrcTag(parseTag(tagsPart, "msg-param-sub-plan-name", ""));
                String originId = parseTag(tagsPart, "msg-param-origin-id", "");
                manager.dispatchMassGiftSub(ch, null, count, tier, planName, originId, 0);
                break;
            }
            case "raid": {
                TwitchUser raider = new TwitchUser(
                        parseTag(tagsPart, "msg-param-login", ""),
                        parseTag(tagsPart, "msg-param-displayName", ""),
                        parseTag(tagsPart, "user-id", ""),
                        parseTag(tagsPart, "color", ""), "",
                        false, false, false, false, false, 0);
                int viewers = parseIntTag(tagsPart, "msg-param-viewerCount", 0);
                String avatar = parseTag(tagsPart, "msg-param-profileImageURL", "");
                manager.dispatchRaid(ch, raider, viewers, avatar);
                break;
            }
            case "announcement": {
                TwitchUser user = userFromTags(tagsPart);
                String color = parseTag(tagsPart, "msg-param-color", "PRIMARY");
                String body = "";
                int colon = rest.indexOf(" :");
                if (colon > -1 && colon + 2 < rest.length()) {
                    body = sanitizeMessage(rest.substring(colon + 2));
                }
                manager.dispatchAnnouncement(ch, user, body, color);
                break;
            }
            default:
                // bitsbadgetier, giftpaidupgrade, anongiftpaidupgrade, ritual, viewermilestone, unraid:
                // silently ignored for now
                break;
        }
    }

    private void handleClearChat(String tagsPart, String rest) {
        String roomId = parseTag(tagsPart, "room-id", "");
        TwitchChannel ch = new TwitchChannel(channelHash, roomId);
        int colon = rest.indexOf(" :");
        String targetLogin = colon > -1 ? rest.substring(colon + 2) : "";

        if (targetLogin.isEmpty()) {
            manager.dispatchClearChat(ch);
        } else {
            int duration = parseIntTag(tagsPart, "ban-duration", 0);
            String targetUserId = parseTag(tagsPart, "target-user-id", "");
            manager.dispatchBan(ch, targetLogin, targetUserId, duration);
        }
    }

    private void handleClearMsg(String tagsPart, String rest) {
        String roomId = parseTag(tagsPart, "room-id", "");
        TwitchChannel ch = new TwitchChannel(channelHash, roomId);
        String login = parseTag(tagsPart, "login", "");
        String targetMsgId = parseTag(tagsPart, "target-msg-id", "");
        int colon = rest.indexOf(" :");
        String body = colon > -1 ? sanitizeMessage(rest.substring(colon + 2)) : "";
        manager.dispatchMessageDelete(ch, login, targetMsgId, body);
    }

    private void handleRoomState(String tagsPart, String rest) {
        String roomId = parseTag(tagsPart, "room-id", "");
        TwitchChannel ch = new TwitchChannel(channelHash, roomId);
        String[] modes = {"slow", "subs-only", "emote-only", "followers-only", "r9k"};
        for (String mode : modes) {
            String val = parseTag(tagsPart, mode, "__missing__");
            if (val.equals("__missing__")) continue;
            try {
                manager.dispatchRoomState(ch, mode, Integer.parseInt(val));
            } catch (NumberFormatException ignored) {}
        }
    }

    private void handleJoin(String rest) {
        // ":login!login@login.tmi.twitch.tv JOIN #channel"
        String login = extractPrefixLogin(rest);
        if (login == null) return;
        if (isSelf(login)) return;            // suppress the bot's own JOIN echo
        TwitchChannel ch = parseChannelFromLine(rest, " JOIN ");
        manager.dispatchUserJoin(ch, login);
    }

    private void handlePart(String rest) {
        String login = extractPrefixLogin(rest);
        if (login == null) return;
        if (isSelf(login)) return;            // suppress the bot's own PART echo
        TwitchChannel ch = parseChannelFromLine(rest, " PART ");
        manager.dispatchUserPart(ch, login);
    }

    private boolean isSelf(String login) {
        return login != null && login.equalsIgnoreCase(nick);
    }

    /** Pulls the "#channel" token that immediately follows the given command marker. */
    private TwitchChannel parseChannelFromLine(String rest, String commandMarker) {
        int idx = rest.indexOf(commandMarker);
        String name = channelHash;
        if (idx >= 0) {
            String after = rest.substring(idx + commandMarker.length());
            int sp = after.indexOf(' ');
            String tok = sp > 0 ? after.substring(0, sp) : after;
            if (tok.startsWith("#")) name = tok;
        }
        return new TwitchChannel(name, "");
    }

    private void handleHostTarget(String rest) {
        // ":tmi.twitch.tv HOSTTARGET #hostingChannel :targetChannel [viewers]"
        int colon = rest.indexOf(" :", rest.indexOf(" HOSTTARGET "));
        if (colon < 0) return;
        String payload = rest.substring(colon + 2).trim();
        String[] parts = payload.split(" ");
        String target = parts.length > 0 ? parts[0] : "-";
        int viewers = -1;
        if (parts.length > 1) {
            try { viewers = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
        }
        TwitchChannel ch = new TwitchChannel(channelHash, "");
        manager.dispatchHost(ch, target, viewers);
    }

    private void handleReconnect() {
        SkTwitch.getInstance().log("&7Twitch requested reconnect for &d#" + channelKey
                + "&7 — reconnecting immediately");
        // Close cleanly; the existing onClose/autoReconnect path takes over.
        try { close(); } catch (Exception ignored) {}
    }

    /** Extracts "login" from ":login!login@login.tmi.twitch.tv ..." */
    private static @Nullable String extractPrefixLogin(String rest) {
        if (!rest.startsWith(":")) return null;
        int bang = rest.indexOf('!');
        if (bang <= 1) return null;
        return rest.substring(1, bang);
    }

    private void handleNotice(String tagsPart, String rest) {
        // Format: ":tmi.twitch.tv NOTICE #channel :body"
        // For pre-login notices (auth failures), the target may be "*" instead
        // of a channel — in that case we dispatch with a null channel.
        String noticeId = parseTag(tagsPart, "msg-id", "");

        String target = "";
        int noticeIdx = rest.indexOf(" NOTICE ");
        if (noticeIdx >= 0) {
            String afterCmd = rest.substring(noticeIdx + " NOTICE ".length());
            int sp = afterCmd.indexOf(' ');
            target = sp > 0 ? afterCmd.substring(0, sp) : afterCmd;
        }

        int colon = rest.indexOf(" :");
        String body = (colon > -1 && colon + 2 <= rest.length()) ? rest.substring(colon + 2) : "";

        TwitchChannel ch;
        if (target.startsWith("#")) {
            String roomId = parseTag(tagsPart, "room-id", "");
            ch = new TwitchChannel(target, roomId);
        } else {
            // Pre-login or global notice — fall back to the channel this socket
            // is bound to so script writers still have a usable bridge context.
            ch = new TwitchChannel(channelHash, "");
        }

        manager.dispatchNotice(ch, noticeId, body);
    }

    private TwitchUser userFromTags(String tagsPart) {
        String login = parseTag(tagsPart, "login", "");
        String displayName = parseTag(tagsPart, "display-name", login);
        String userId = parseTag(tagsPart, "user-id", "");
        String color = parseTag(tagsPart, "color", "");
        String badges = parseTag(tagsPart, "badges", "");
        String badgeInfo = parseTag(tagsPart, "badge-info", "");
        String userType = parseTag(tagsPart, "user-type", "");
        boolean founder = badges.contains("founder/");
        boolean sub = badges.contains("subscriber/") || founder;
        boolean mod = badges.contains("moderator/") || "1".equals(parseTag(tagsPart, "mod", "0"));
        boolean caster = badges.contains("broadcaster/");
        boolean vip = badges.contains("vip/") || !parseTag(tagsPart, "vip", "").isEmpty();
        boolean turbo = badges.contains("turbo/");
        int exact = 0;
        if (!badgeInfo.isEmpty()) {
            for (String info : badgeInfo.split(",")) {
                if (info.startsWith("subscriber/") || info.startsWith("founder/")) {
                    try { exact = Integer.parseInt(info.substring(info.indexOf('/') + 1)); }
                    catch (NumberFormatException ignored) {}
                    break;
                }
            }
        }
        return new TwitchUser(login, displayName, userId, color, badges, badgeInfo, userType,
                sub, mod, caster, vip, turbo, founder, 0, exact);
    }

    private TwitchUser stubUser(String login, String displayName, String userId) {
        return new TwitchUser(login, displayName, userId, null, "", "", "",
                false, false, false, false, false, false, 0, 0);
    }

    private int parseIntTag(String tagsPart, String key, int fallback) {
        try { return Integer.parseInt(parseTag(tagsPart, key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private long parseLongTag(String tagsPart, String key, long fallback) {
        try { return Long.parseLong(parseTag(tagsPart, key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private int parseHypeLevel(String level) {
        // Twitch uses "ONE", "TWO", ..., "TEN" - try numeric first, then map word -> n
        try { return Integer.parseInt(level); } catch (NumberFormatException ignored) {}
        return switch (level.toUpperCase()) {
            case "ONE" -> 1;
            case "TWO" -> 2;
            case "THREE" -> 3;
            case "FOUR" -> 4;
            case "FIVE" -> 5;
            case "SIX" -> 6;
            case "SEVEN" -> 7;
            case "EIGHT" -> 8;
            case "NINE" -> 9;
            case "TEN" -> 10;
            default -> 0;
        };
    }

    private String parseTag(String tags, String key, String fallback) {
        if (tags.isEmpty()) return fallback;
        for (String tag : tags.split(";")) {
            String[] kv = tag.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key) && !kv[1].isEmpty()) return kv[1];
        }
        return fallback;
    }

    private String unescapeIrcTag(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 's' -> sb.append(' ');
                    case ':' -> sb.append(';');
                    case 'r' -> sb.append('\r');
                    case 'n' -> sb.append('\n');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String sanitizeMessage(String s) {
        if (s == null || s.isEmpty()) return s;
        s = com.vdurmont.emoji.EmojiParser.parseToAliases(s);
        return s.replaceAll("\\p{So}|\\p{Cn}|\\p{Cf}", "")
                .trim()
                .replaceAll(" {2,}", " ");
    }

    private String wrapTwitchEmotes(String message, String emotesTag) {
        if (emotesTag == null || emotesTag.isEmpty()) return message;

        record Range(int start, int end) {}
        List<Range> ranges = new ArrayList<>();
        for (String emote : emotesTag.split("/")) {
            int colon = emote.indexOf(':');
            if (colon < 0) continue;
            for (String range : emote.substring(colon + 1).split(",")) {
                String[] parts = range.split("-");
                if (parts.length != 2) continue;
                try {
                    ranges.add(new Range(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                } catch (NumberFormatException ignored) {}
            }
        }
        ranges.sort((a, b) -> Integer.compare(b.start, a.start));

        StringBuilder sb = new StringBuilder(message);
        int[] codepoints = message.codePoints().toArray();
        for (Range r : ranges) {
            if (r.start < 0 || r.end >= codepoints.length) continue;
            try {
                int charStart = message.offsetByCodePoints(0, r.start);
                int charEnd = message.offsetByCodePoints(0, r.end + 1);
                String name = sb.substring(charStart, charEnd);
                sb.replace(charStart, charEnd, ":" + name + ":");
            } catch (IndexOutOfBoundsException ignored) {}
        }
        return sb.toString();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (shutdownRequested.get() || !autoReconnect) return;

        reconnectAttempts++;
        long delay = Math.min(60L, (long) Math.pow(2, Math.min(reconnectAttempts, 6))) * 1000L;
        SkTwitch.getInstance().log("&eDisconnected from &d#" + channelKey
                + "&e (code=" + code + "). Reconnecting in &b" + (delay / 1000) + "s&e...");

        Bukkit.getScheduler().runTaskLaterAsynchronously(SkTwitch.getInstance(), () -> {
            if (shutdownRequested.get()) return;
            try {
                this.reconnect();
            } catch (Exception ex) {
                SkTwitch.getInstance().log("&cReconnect failed for &d#" + channelKey + "&c: " + ex.getMessage());
            }
        }, delay / 50L);
    }

    @Override
    public void onError(Exception e) {
        SkTwitch.getInstance().log("&eWebSocket error on &d#" + channelKey + "&e: " + e.getMessage());
    }

    public void shutdown() {
        shutdownRequested.set(true);
        close();
    }
}
