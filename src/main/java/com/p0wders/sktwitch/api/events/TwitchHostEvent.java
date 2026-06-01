package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired on Twitch HOSTTARGET. Twitch has largely deprecated the host feature
 * in the UI but the IRC command still fires in some cases. When hosting stops,
 * {@code targetChannel} is "-" and {@code viewers} is -1.
 */
public class TwitchHostEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String targetChannel;   // "-" when hosting stops
    private final int viewers;             // -1 when unknown / stop event

    public TwitchHostEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                           @NotNull String targetChannel, int viewers) {
        super(channel, bridgeName);
        this.targetChannel = targetChannel;
        this.viewers = viewers;
    }

    public @NotNull String getTargetChannel() { return targetChannel; }
    public int getViewers() { return viewers; }
    public boolean isHostStop() { return "-".equals(targetChannel); }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}