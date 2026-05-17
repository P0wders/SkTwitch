package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchRoomStateEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /** "slow", "subs-only", "emote-only", "followers-only", "r9k" */
    private final String mode;
    /** For boolean modes: 0=off, 1=on. For duration modes: -1=off, 0=immediate, N=seconds/minutes. */
    private final int value;

    public TwitchRoomStateEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                                @NotNull String mode, int value) {
        super(channel, bridgeName);
        this.mode = mode;
        this.value = value;
    }

    public @NotNull String getMode() { return mode; }
    public int getValue() { return value; }
    public boolean isEnabled() { return value > 0 || (value == 0 && mode.equals("followers-only")); }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}