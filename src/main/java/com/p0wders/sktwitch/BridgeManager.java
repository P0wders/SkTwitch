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

    /** Per-bridge user caches. Key: bridge name. */
    private final Map<String, com.p0wders.sktwitch.cache.TwitchUserCache> userCaches = new HashMap<>();
    /** Per-bridge channel caches. */
    private final Map<String, com.p0wders.sktwitch.cache.TwitchChannelCache> channelCaches = new HashMap<>();

    public BridgeManager(SkTwitch plugin) {
        this.plugin = plugin;
    }

    public synchronized void register(Bridge bridge) {
        Bridge oldBridge = bridges.get(bridge.name());
        bridges.put(bridge.name(), bridge);
        userCaches.computeIfAbsent(bridge.name(), k -> new com.p0wders.sktwitch.cache.TwitchUserCache());
        channelCaches.computeIfAbsent(bridge.name(), k -> new com.p0wders.sktwitch.cache.TwitchChannelCache());

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
        userCaches.remove(bridgeName);
        channelCaches.remove(bridgeName);
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
        boolean tags = false, commands = false, membership = false, reconnect = false;
        String oauth = null, nick = null;

        for (String bridgeName : owners) {
            Bridge b = bridges.get(bridgeName);
            if (b == null) continue;
            if (b.requestTags()) tags = true;
            if (b.requestCommands()) commands = true;
            if (b.requestMembership()) membership = true;
            if (b.autoReconnect()) reconnect = true;
            if (b.oauthToken() != null && oauth == null) {
                oauth = b.oauthToken();
                nick = b.nickname();
            } else if (b.oauthToken() != null && !b.oauthToken().equals(oauth)) {
                plugin.log("&eWarning: channel &d#" + channel
                        + "&e is referenced by bridges with conflicting OAuth tokens; using first.");
            }
        }

        TwitchClient client = new TwitchClient(channel, oauth, nick, reconnect, tags, commands, membership, this);

        connections.put(channel, client);
        client.connect();
        plugin.log("Connecting to &d#" + channel);
    }

    public void dispatch(TwitchChannel channel, TwitchUser user, String message,
                         String messageId, boolean isFirstMessage, boolean isReturningChatter,
                         boolean isReply, @Nullable TwitchUser replyParentUser,
                         String replyParentMessage, String replyParentMessageId,
                         @Nullable TwitchUser threadParentUser, String threadParentMessageId,
                         long timestamp, String customRewardId, String autoModFlags,
                         int hypeChatAmount, String hypeChatCurrency, int hypeChatLevel,
                         boolean hypeChatSystem, String sourceRoomId, String sourceMessageId) {
        fire(channel, bridge -> new TwitchMessageEvent(
                user, channel, message, messageId, bridge, isFirstMessage, isReturningChatter,
                isReply, replyParentUser, replyParentMessage, replyParentMessageId,
                threadParentUser, threadParentMessageId,
                timestamp, customRewardId, autoModFlags,
                hypeChatAmount, hypeChatCurrency, hypeChatLevel, hypeChatSystem,
                sourceRoomId, sourceMessageId));
    }

    public void dispatchSubscribe(TwitchChannel ch, TwitchUser user, String tier, String planName,
                                  int cumulative, int streak, int multiDur, int multiTen,
                                  boolean resub, @Nullable String message) {
        fire(ch, bridge -> new TwitchSubscribeEvent(ch, bridge, user, tier, planName,
                cumulative, streak, multiDur, multiTen, resub, message));
    }

    public void dispatchGiftSub(TwitchChannel ch, @Nullable TwitchUser gifter, TwitchUser recipient,
                                String tier, String planName, int recipientTotalMonths,
                                int giftDurationMonths, String originId, int senderTotalGifts) {
        fire(ch, bridge -> new TwitchGiftSubEvent(ch, bridge, gifter, recipient, tier, planName,
                recipientTotalMonths, giftDurationMonths, originId, senderTotalGifts));
    }

    public void dispatchMassGiftSub(TwitchChannel ch, @Nullable TwitchUser gifter, int count, String tier,
                                    String planName, String originId, int senderTotalGifts) {
        fire(ch, bridge -> new TwitchMassGiftSubEvent(ch, bridge, gifter, count, tier,
                planName, originId, senderTotalGifts));
    }

    public void dispatchRaid(TwitchChannel ch, TwitchUser raider, int viewers, String profileImageUrl) {
        fire(ch, bridge -> new TwitchRaidEvent(ch, bridge, raider, viewers, profileImageUrl));
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

    public void dispatchNotice(TwitchChannel ch, String noticeId, String message) {
        fire(ch, bridge -> new TwitchNoticeEvent(ch, bridge, noticeId, message));
    }

    public void dispatchCheer(TwitchChannel ch, TwitchUser user, int bits, String message) {
        fire(ch, bridge -> new TwitchCheerEvent(ch, bridge, user, bits, message));
    }

    public void dispatchUserJoin(TwitchChannel ch, String login) {
        fire(ch, bridge -> new TwitchUserJoinEvent(ch, bridge, login));
    }

    public void dispatchUserPart(TwitchChannel ch, String login) {
        fire(ch, bridge -> new TwitchUserPartEvent(ch, bridge, login));
    }

    public void dispatchHost(TwitchChannel ch, String targetChannel, int viewers) {
        fire(ch, bridge -> new TwitchHostEvent(ch, bridge, targetChannel, viewers));
    }

    // --- cache feeders, called by TwitchClient on every parsed event ---

    /**
     * Records a user into every cache owned by a bridge that subscribes to
     * the given channel. Safe to call from the IRC reader thread.
     */
    public void cacheUser(TwitchChannel channel, TwitchUser user) {
        if (user == null) return;
        Set<String> owners;
        synchronized (this) {
            owners = channelToBridges.get(channel.getNameWithoutHash().toLowerCase());
            if (owners == null) return;
            owners = new HashSet<>(owners);
        }
        for (String bridgeName : owners) {
            var cache = userCaches.get(bridgeName);
            if (cache != null) cache.put(user);
        }
    }

    /** Same idea for channels. */
    public void cacheChannel(TwitchChannel channel) {
        if (channel == null) return;
        Set<String> owners;
        synchronized (this) {
            owners = channelToBridges.get(channel.getNameWithoutHash().toLowerCase());
            if (owners == null) return;
            owners = new HashSet<>(owners);
        }
        for (String bridgeName : owners) {
            var cache = channelCaches.get(bridgeName);
            if (cache != null) cache.put(channel);
        }
    }

    // --- introspection / lookup APIs used by Skript expressions ---

    public @Nullable TwitchUser lookupUser(String login, @Nullable String bridgeName) {
        if (bridgeName != null) {
            var cache = userCaches.get(bridgeName);
            return cache == null ? null : cache.get(login);
        }
        for (var cache : userCaches.values()) {
            TwitchUser u = cache.get(login);
            if (u != null) return u;
        }
        return null;
    }

    public @Nullable TwitchChannel lookupChannel(String name, @Nullable String bridgeName) {
        if (bridgeName != null) {
            var cache = channelCaches.get(bridgeName);
            return cache == null ? null : cache.get(name);
        }
        for (var cache : channelCaches.values()) {
            TwitchChannel c = cache.get(name);
            if (c != null) return c;
        }
        return null;
    }

    public Collection<TwitchUser> allCachedUsers(@Nullable String bridgeName) {
        if (bridgeName != null) {
            var cache = userCaches.get(bridgeName);
            return cache == null ? List.of() : cache.all();
        }
        List<TwitchUser> all = new ArrayList<>();
        for (var cache : userCaches.values()) all.addAll(cache.all());
        return all;
    }

    public Collection<TwitchChannel> allCachedChannels(@Nullable String bridgeName) {
        if (bridgeName != null) {
            var cache = channelCaches.get(bridgeName);
            return cache == null ? List.of() : cache.all();
        }
        List<TwitchChannel> all = new ArrayList<>();
        for (var cache : channelCaches.values()) all.addAll(cache.all());
        return all;
    }

    public synchronized Collection<String> registeredBridges() {
        return new ArrayList<>(bridges.keySet());
    }

    /**
     * A bridge counts as connected if at least one of its channels currently
     * has a live WebSocket and that socket reports as open.
     */
    public synchronized Collection<String> connectedBridges() {
        List<String> out = new ArrayList<>();
        for (var entry : bridges.entrySet()) {
            if (isBridgeConnectedInternal(entry.getValue())) out.add(entry.getKey());
        }
        return out;
    }

    public synchronized boolean isBridgeConnected(String bridgeName) {
        Bridge b = bridges.get(bridgeName);
        return b != null && isBridgeConnectedInternal(b);
    }

    private boolean isBridgeConnectedInternal(Bridge b) {
        for (String channel : b.channels()) {
            TwitchClient client = connections.get(channel);
            if (client != null && client.isOpen()) return true;
        }
        return false;
    }

    public synchronized Collection<String> channelsOfBridge(String bridgeName) {
        Bridge b = bridges.get(bridgeName);
        if (b == null) return List.of();
        List<String> out = new ArrayList<>(b.channels().size());
        for (String ch : b.channels()) out.add("#" + ch);
        return out;
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
