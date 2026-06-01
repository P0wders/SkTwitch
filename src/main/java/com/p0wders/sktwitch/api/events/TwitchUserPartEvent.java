package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when Twitch sends a PART command for a user leaving a channel.
 * Same caveats as {@link TwitchUserJoinEvent} — requires the membership
 * capability and is suppressed on channels >~1000 users.
 */
public class TwitchUserPartEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String login;

    public TwitchUserPartEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                               @NotNull String login) {
        super(channel, bridgeName);
        this.login = login;
    }

    public @NotNull String getLogin() { return login; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}