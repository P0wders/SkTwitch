package com.p0wders.sktwitch;

import com.p0wders.sktwitch.api.events.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class BridgeManager {

    private final SkTwitch plugin;
    private final Map<String, Bridge> bridges = new HashMap<>();
    private final Map<String, TwitchClient> connections = new HashMap<>();
    private final Map<String, Set<String>> channelToBridges = new HashMap<>();

    public BridgeManager(SkTwitch plugin) {
        this.plugin = plugin;
    }

    public synchronized void register(Bridge bridge) {
        Bridge oldBridge = bridges.get(bridge.name());
        bridges.put(bridge.name(), bridge);

        Set<String> oldChannels = oldBridge != null ? new HashSet<>(oldBridge.channels()) : Set.of();
        Set<String> newChannels = new HashSet<>(bridge.channels());

        Set<String> removed = new HashSet<>(oldChannels);
        removed.removeAll(newChannels);
        for (String channel : removed) {
            releaseChannel(channel, bridge.name());
        }

        Set<String> added = new HashSet<>(newChannels);
        added.removeAll(oldChannels);
        for (String channel : added) {
            channelToBridges.computeIfAbsent(channel, k -> new HashSet<>()).add(bridge.name());
            ensureConnection(channel);
        }

        if (oldBridge == null) {
            plugin.log("Bridge &e'" + bridge.name() + "'&r registered (&d" + newChannels.size() + "&r channel(s))");
        } else {
            int kept = newChannels.size() - added.size();
            plugin.log("Bridge &e'" + bridge.name() + "'&r updated: &a" + kept + " kept&r, &b"
                    + added.size() + " added&r, &c" + removed.size() + " removed");
        }
    }

    public synchronized void unregister(String bridgeName) {
        Bridge bridge = bridges.remove(bridgeName);
        if (bridge == null) return;

        for (String channel : bridge.channels()) {
            releaseChannel(channel, bridgeName);
        }
        plugin.log("Bridge &e'" + bridgeName + "'&r unregistered");
    }

    private void releaseChannel(String channel, String bridgeName) {
        Set<String> owners = channelToBridges.get(channel);
        if (owners == null) return;
        owners.remove(bridgeName);
        if (owners.isEmpty()) {
            channelToBridges.remove(channel);
            TwitchClient client = connections.remove(channel);
            if (client != null) {
                client.shutdown();
                plugin.log("&7Closed connection to &d#" + channel);
            }
        }
    }

    private void ensureConnection(String channel) {
        if (connections.containsKey(channel)) return;

        Set<String> owners = channelToBridges.get(channel);
        boolean tags = false, commands = false, reconnect = false;
        String oauth = null, nick = null;

        for (String bridgeName : owners) {
            Bridge b = bridges.get(bridgeName);
            if (b == null) continue;
            if (b.requestTags()) tags = true;
            if (b.requestCommands()) commands = true;
            if (b.autoReconnect()) reconnect = true;
            if (b.oauthToken() != null && oauth == null) {
                oauth = b.oauthToken();
                nick = b.nickname();
            } else if (b.oauthToken() != null && !b.oauthToken().equals(oauth)) {
                plugin.log("&eWarning: channel &d#" + channel
                        + "&e is referenced by bridges with conflicting OAuth tokens; using first.");
            }
        }

        TwitchClient client = new TwitchClient(channel, oauth, nick, reconnect, tags, commands, this);
        connections.put(channel, client);
        client.connect();
        plugin.log("Connecting to &d#" + channel);
    }

    public void dispatch(TwitchChannel channel, TwitchUser user, String message,
                         String messageId, boolean isFirstMessage,
                         boolean isReply, @Nullable TwitchUser replyParentUser,
                         String replyParentMessage, String replyParentMessageId) {
        fire(channel, bridge -> new TwitchMessageEvent(
                user, channel, message, messageId, bridge, isFirstMessage,
                isReply, replyParentUser, replyParentMessage, replyParentMessageId));
    }

    public void dispatchSubscribe(TwitchChannel ch, TwitchUser user, String tier,
                                  int cumulative, int streak, boolean resub, @Nullable String message) {
        fire(ch, bridge -> new TwitchSubscribeEvent(ch, bridge, user, tier, cumulative, streak, resub, message));
    }

    public void dispatchGiftSub(TwitchChannel ch, @Nullable TwitchUser gifter, TwitchUser recipient,
                                String tier, int months) {
        fire(ch, bridge -> new TwitchGiftSubEvent(ch, bridge, gifter, recipient, tier, months));
    }

    public void dispatchMassGiftSub(TwitchChannel ch, @Nullable TwitchUser gifter, int count, String tier) {
        fire(ch, bridge -> new TwitchMassGiftSubEvent(ch, bridge, gifter, count, tier));
    }

    public void dispatchRaid(TwitchChannel ch, TwitchUser raider, int viewers) {
        fire(ch, bridge -> new TwitchRaidEvent(ch, bridge, raider, viewers));
    }

    public void dispatchAnnouncement(TwitchChannel ch, TwitchUser user, String message, String color) {
        fire(ch, bridge -> new TwitchAnnouncementEvent(ch, bridge, user, message, color));
    }

    public void dispatchBan(TwitchChannel ch, String targetLogin, String targetUserId, int durationSeconds) {
        fire(ch, bridge -> new TwitchBanEvent(ch, bridge, targetLogin, targetUserId, durationSeconds));
    }

    public void dispatchClearChat(TwitchChannel ch) {
        fire(ch, bridge -> new TwitchClearChatEvent(ch, bridge));
    }

    public void dispatchMessageDelete(TwitchChannel ch, String login, String msgId, String body) {
        fire(ch, bridge -> new TwitchMessageDeleteEvent(ch, bridge, login, msgId, body));
    }

    public void dispatchRoomState(TwitchChannel ch, String mode, int value) {
        fire(ch, bridge -> new TwitchRoomStateEvent(ch, bridge, mode, value));
    }

    public void dispatchCheer(TwitchChannel ch, TwitchUser user, int bits, String message) {
        fire(ch, bridge -> new TwitchCheerEvent(ch, bridge, user, bits, message));
    }

    private void fire(TwitchChannel channel, Function<String, ? extends Event> factory) {
        Set<String> owners = channelToBridges.get(channel.getNameWithoutHash().toLowerCase());
        if (owners == null || owners.isEmpty()) return;
        for (String bridgeName : owners) {
            Event event = factory.apply(bridgeName);
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
        }
    }

    public synchronized void shutdownAll() {
        for (TwitchClient client : connections.values()) {
            client.shutdown();
        }
        connections.clear();
        bridges.clear();
        channelToBridges.clear();
    }
}