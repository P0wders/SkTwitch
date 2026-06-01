package com.p0wders.sktwitch.cache;

import com.p0wders.sktwitch.TwitchChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of {@link TwitchChannel} objects keyed by lowercase name
 * (with or without leading #). Populated by ROOMSTATE / USERNOTICE / PRIVMSG
 * — whichever IRC command first carries the room-id.
 */
public class TwitchChannelCache {

    private final ConcurrentHashMap<String, TwitchChannel> channels = new ConcurrentHashMap<>();

    public void put(@Nullable TwitchChannel channel) {
        if (channel == null) return;
        String name = channel.getName();
        if (name == null || name.isEmpty()) return;
        channels.put(normalize(name), channel);
    }

    public @Nullable TwitchChannel get(@Nullable String name) {
        if (name == null || name.isEmpty()) return null;
        return channels.get(normalize(name));
    }

    public Collection<TwitchChannel> all() {
        return Collections.unmodifiableCollection(channels.values());
    }

    public int size() { return channels.size(); }

    public void clear() { channels.clear(); }

    private static String normalize(String name) {
        String n = name.startsWith("#") ? name.substring(1) : name;
        return n.toLowerCase(Locale.ROOT);
    }
}