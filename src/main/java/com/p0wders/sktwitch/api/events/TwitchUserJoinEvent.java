package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when Twitch sends a JOIN command for a user entering a channel.
 *
 * Requires the {@code twitch.tv/membership} capability to be requested.
 * Twitch suppresses JOIN/PART entirely on channels with more than ~1000
 * concurrent users, so this event is only practically useful for small
 * channels. The login is the only data Twitch provides on JOIN — no badges,
 * no display name capitalization, no user-id.
 */
public class TwitchUserJoinEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String login;

    public TwitchUserJoinEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                               @NotNull String login) {
        super(channel, bridgeName);
        this.login = login;
    }

    public @NotNull String getLogin() { return login; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}