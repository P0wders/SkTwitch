package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TwitchGiftSubEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final @Nullable TwitchUser gifter;  // null if anonymous gift
    private final TwitchUser recipient;
    private final String tier;
    private final int months;                   // gift duration in months

    public TwitchGiftSubEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                              @Nullable TwitchUser gifter, @NotNull TwitchUser recipient,
                              @NotNull String tier, int months) {
        super(channel, bridgeName);
        this.gifter = gifter;
        this.recipient = recipient;
        this.tier = tier;
        this.months = months;
    }

    public @Nullable TwitchUser getGifter() { return gifter; }
    public @NotNull TwitchUser getRecipient() { return recipient; }
    public @NotNull String getTier() { return tier; }
    public int getMonths() { return months; }
    public boolean isAnonymous() { return gifter == null; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}