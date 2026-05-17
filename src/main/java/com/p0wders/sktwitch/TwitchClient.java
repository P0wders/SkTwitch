package com.p0wders.sktwitch;

import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
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
            if (!line.contains("PRIVMSG")) continue;

            String tagsPart = "";
            String rest = line;
            if (line.startsWith("@")) {
                int sp = line.indexOf(' ');
                tagsPart = line.substring(1, sp);
                rest = line.substring(sp + 1);
            }

            String user = rest.split("!", 2)[0].substring(1);
            String[] split = rest.split("PRIVMSG", 2);
            if (split.length < 2) continue;
            String[] msgSplit = split[1].split(":", 2);
            if (msgSplit.length < 2) continue;
            String message = msgSplit[1];

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

            manager.dispatch(channelHash, user, displayName, message, color,
                    userId, messageId, roomId, badges,
                    isSubscriber, isModerator, isBroadcaster, isVip, isTurbo, isFirstMessage, subMonths);
        }
    }

    private String parseTag(String tags, String key, String fallback) {
        if (tags.isEmpty()) return fallback;
        for (String tag : tags.split(";")) {
            String[] kv = tag.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key) && !kv[1].isEmpty()) return kv[1];
        }
        return fallback;
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