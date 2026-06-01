package com.p0wders.sktwitch.cache;

import com.p0wders.sktwitch.TwitchUser;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of {@link TwitchUser} objects, keyed by lowercase login.
 *
 * Scope: per-bridge. The cache is populated as a side effect of every inbound
 * event (PRIVMSG, USERNOTICE, CLEARCHAT, CLEARMSG, etc.) that carries user
 * identity. A user who has never appeared in any event will not be present —
 * IRC has no concept of "look up an arbitrary user", that requires Helix.
 *
 * The cache is intentionally unbounded: a busy stream has on the order of
 * thousands of unique chatters per session, which is trivial memory. If you
 * ever need to bound it, swap the backing map for a Caffeine cache.
 */
public class TwitchUserCache {

    private final ConcurrentHashMap<String, TwitchUser> users = new ConcurrentHashMap<>();

    /** Stores or refreshes a user. Subsequent appearances overwrite, so the
     *  cache always reflects the most recently observed state (badges, sub
     *  months, color, etc. can change between messages). */
    public void put(@Nullable TwitchUser user) {
        if (user == null) return;
        String login = user.getUsername();
        if (login == null || login.isEmpty()) return;
        users.put(login.toLowerCase(Locale.ROOT), user);
    }

    /** Lookup by login (case-insensitive). Returns null if never seen. */
    public @Nullable TwitchUser get(@Nullable String login) {
        if (login == null || login.isEmpty()) return null;
        // strip @ prefix if scripts pass "@username"
        String key = login.startsWith("@") ? login.substring(1) : login;
        return users.get(key.toLowerCase(Locale.ROOT));
    }

    /** Snapshot of all known users for this bridge. */
    public Collection<TwitchUser> all() {
        return Collections.unmodifiableCollection(users.values());
    }

    public int size() { return users.size(); }

    public void clear() { users.clear(); }
}