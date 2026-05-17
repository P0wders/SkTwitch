package com.p0wders.sktwitch;

import com.p0wders.sktwitch.api.events.TwitchMessageEvent;
import org.bukkit.Bukkit;

import java.util.*;

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
                         boolean isReply, TwitchUser replyParentUser,
                         String replyParentMessage, String replyParentMessageId) {
        Set<String> owners = channelToBridges.get(channel.getNameWithoutHash().toLowerCase());
        if (owners == null || owners.isEmpty()) return;

        for (String bridgeName : owners) {
            TwitchMessageEvent event = new TwitchMessageEvent(
                    user, channel, message, messageId, bridgeName, isFirstMessage,
                    isReply, replyParentUser, replyParentMessage, replyParentMessageId);
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