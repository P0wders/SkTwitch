package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchRaidEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final TwitchUser raider;
    private final int viewerCount;
    private final String profileImageUrl;

    public TwitchRaidEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                           @NotNull TwitchUser raider, int viewerCount,
                           @NotNull String profileImageUrl) {
        super(channel, bridgeName);
        this.raider = raider;
        this.viewerCount = viewerCount;
        this.profileImageUrl = profileImageUrl;
    }

    public @NotNull TwitchUser getRaider() { return raider; }
    public int getViewerCount() { return viewerCount; }
    public @NotNull String getProfileImageUrl() { return profileImageUrl; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
