package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchAnnouncementEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final TwitchUser user;
    private final String message;
    private final String color;  // "PRIMARY", "BLUE", "GREEN", "ORANGE", "PURPLE"

    public TwitchAnnouncementEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                                   @NotNull TwitchUser user, @NotNull String message,
                                   @NotNull String color) {
        super(channel, bridgeName);
        this.user = user;
        this.message = message;
        this.color = color;
    }

    public @NotNull TwitchUser getUser() { return user; }
    public @NotNull String getMessage() { return message; }
    public @NotNull String getColor() { return color; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}