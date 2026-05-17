package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TwitchMassGiftSubEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final @Nullable TwitchUser gifter;
    private final int count;
    private final String tier;

    public TwitchMassGiftSubEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                                  @Nullable TwitchUser gifter, int count, @NotNull String tier) {
        super(channel, bridgeName);
        this.gifter = gifter;
        this.count = count;
        this.tier = tier;
    }

    public @Nullable TwitchUser getGifter() { return gifter; }
    public int getCount() { return count; }
    public @NotNull String getTier() { return tier; }
    public boolean isAnonymous() { return gifter == null; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}