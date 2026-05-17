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
    private final BridgeManager manager;

    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private int reconnectAttempts = 0;

    public TwitchClient(String channel, @Nullable String oauthToken, @Nullable String nickname,
                        boolean autoReconnect, boolean requestTags, boolean requestCommands,
                        BridgeManager manager) {
        super(URI.create("wss://irc-ws.chat.twitch.tv:443"));
        this.channelKey = channel.toLowerCase().replace("#", "");
        this.channelHash = "#" + channelKey;
        this.oauthToken = oauthToken;
        this.nick = nickname != null ? nickname : "justinfan" + (10000 + new Random().nextInt(90000));
        this.autoReconnect = autoReconnect;
        this.requestTags = requestTags;
        this.requestCommands = requestCommands;
        this.manager = manager;
    }

    @Override
    public void onOpen(ServerHandshake h) {
        reconnectAttempts = 0;
        StringBuilder caps = new StringBuilder("CAP REQ :");
        if (requestTags) caps.append("twitch.tv/tags ");
        if (requestCommands) caps.append("twitch.tv/commands ");
        if (!caps.toString().equals("CAP REQ :")) {
            send(caps.toString().trim());
        }
        if (oauthToken != null) {
            send("PASS " + (oauthToken.startsWith("oauth:") ? oauthToken : "oauth:" + oauthToken));
        }
        send("NICK " + nick);
        send("JOIN " + channelHash);
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
            if (rest.contains(" ROOMSTATE ")) {
                handleRoomState(tagsPart, rest);
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

            boolean isSubscriber = badges.contains("subscriber/") || badges.contains("founder/");
            boolean isModerator = badges.contains("moderator/") || "1".equals(parseTag(tagsPart, "mod", "0"));
            boolean isBroadcaster = badges.contains("broadcaster/");
            boolean isVip = badges.contains("vip/");
            boolean isTurbo = badges.contains("turbo/");
            boolean isFirstMessage = "1".equals(parseTag(tagsPart, "first-msg", "0"));

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

            String replyParentMsgId = parseTag(tagsPart, "reply-parent-msg-id", "");
            String replyParentUserId = parseTag(tagsPart, "reply-parent-user-id", "");
            String replyParentLogin = parseTag(tagsPart, "reply-parent-user-login", "");
            String replyParentDisplayName = parseTag(tagsPart, "reply-parent-display-name", "");
            String replyParentBody = parseTag(tagsPart, "reply-parent-msg-body", "");

            boolean isReply = !replyParentMsgId.isEmpty();
            TwitchUser replyParentUser = null;
            String replyParentMessage = "";
            if (isReply) {
                replyParentUser = new TwitchUser(
                        replyParentLogin, replyParentDisplayName, replyParentUserId,
                        null, "", false, false, false, false, false, 0);
                replyParentMessage = sanitizeMessage(unescapeIrcTag(replyParentBody));
            }

            TwitchUser twitchUser = new TwitchUser(user, displayName, userId, color, badges,
                    isSubscriber, isModerator, isBroadcaster, isVip, isTurbo, subMonths);
            TwitchChannel twitchChannel = new TwitchChannel(channelHash, roomId);

            int bits = parseIntTag(tagsPart, "bits", 0);
            if (bits > 0) {
                manager.dispatchCheer(twitchChannel, twitchUser, bits, message);
            }

            manager.dispatch(twitchChannel, twitchUser, message, messageId, isFirstMessage,
                    isReply, replyParentUser, replyParentMessage, replyParentMsgId);
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
                int cumulative = parseIntTag(tagsPart, "msg-param-cumulative-months", 1);
                int streak = "1".equals(parseTag(tagsPart, "msg-param-should-share-streak", "0"))
                        ? parseIntTag(tagsPart, "msg-param-streak-months", 0) : 0;
                String body = null;
                int colon = rest.indexOf(" :");
                if (colon > -1 && colon + 2 < rest.length()) {
                    body = sanitizeMessage(rest.substring(colon + 2));
                }
                manager.dispatchSubscribe(ch, user, tier, cumulative, streak, "resub".equals(msgId), body);
                break;
            }
            case "subgift": {
                TwitchUser gifter = userFromTags(tagsPart);
                TwitchUser recipient = new TwitchUser(
                        parseTag(tagsPart, "msg-param-recipient-user-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-display-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-id", ""),
                        null, "", false, false, false, false, false, 0);
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                int months = parseIntTag(tagsPart, "msg-param-months", 1);
                manager.dispatchGiftSub(ch, gifter, recipient, tier, months);
                break;
            }
            case "anonsubgift": {
                TwitchUser recipient = new TwitchUser(
                        parseTag(tagsPart, "msg-param-recipient-user-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-display-name", ""),
                        parseTag(tagsPart, "msg-param-recipient-id", ""),
                        null, "", false, false, false, false, false, 0);
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                int months = parseIntTag(tagsPart, "msg-param-months", 1);
                manager.dispatchGiftSub(ch, null, recipient, tier, months);
                break;
            }
            case "submysterygift": {
                TwitchUser gifter = userFromTags(tagsPart);
                int count = parseIntTag(tagsPart, "msg-param-mass-gift-count", 1);
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                manager.dispatchMassGiftSub(ch, gifter, count, tier);
                break;
            }
            case "anonsubmysterygift": {
                int count = parseIntTag(tagsPart, "msg-param-mass-gift-count", 1);
                String tier = parseTag(tagsPart, "msg-param-sub-plan", "1000");
                manager.dispatchMassGiftSub(ch, null, count, tier);
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
                manager.dispatchRaid(ch, raider, viewers);
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

    private TwitchUser userFromTags(String tagsPart) {
        String login = parseTag(tagsPart, "login", "");
        String displayName = parseTag(tagsPart, "display-name", login);
        String userId = parseTag(tagsPart, "user-id", "");
        String color = parseTag(tagsPart, "color", "");
        String badges = parseTag(tagsPart, "badges", "");
        boolean sub = badges.contains("subscriber/") || badges.contains("founder/");
        boolean mod = badges.contains("moderator/") || "1".equals(parseTag(tagsPart, "mod", "0"));
        boolean caster = badges.contains("broadcaster/");
        boolean vip = badges.contains("vip/");
        boolean turbo = badges.contains("turbo/");
        return new TwitchUser(login, displayName, userId, color, badges, sub, mod, caster, vip, turbo, 0);
    }

    private int parseIntTag(String tagsPart, String key, int fallback) {
        try { return Integer.parseInt(parseTag(tagsPart, key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
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