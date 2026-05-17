package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchCheerEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final TwitchUser user;
    private final int bits;
    private final String message;

    public TwitchCheerEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                            @NotNull TwitchUser user, int bits, @NotNull String message) {
        super(channel, bridgeName);
        this.user = user;
        this.bits = bits;
        this.message = message;
    }

    public @NotNull TwitchUser getUser() { return user; }
    public int getBits() { return bits; }
    public @NotNull String getMessage() { return message; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}