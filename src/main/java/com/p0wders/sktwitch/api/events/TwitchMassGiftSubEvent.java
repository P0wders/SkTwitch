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
    private final String subPlanName;
    private final String originId;          // id assigned to this mass-gift batch
    private final int senderTotalGifts;     // msg-param-sender-count

    public TwitchMassGiftSubEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                                  @Nullable TwitchUser gifter, int count, @NotNull String tier,
                                  @NotNull String subPlanName, @NotNull String originId,
                                  int senderTotalGifts) {
        super(channel, bridgeName);
        this.gifter = gifter;
        this.count = count;
        this.tier = tier;
        this.subPlanName = subPlanName;
        this.originId = originId;
        this.senderTotalGifts = senderTotalGifts;
    }

    public @Nullable TwitchUser getGifter() { return gifter; }
    public int getCount() { return count; }
    public @NotNull String getTier() { return tier; }
    public @NotNull String getSubPlanName() { return subPlanName; }
    public @NotNull String getOriginId() { return originId; }
    public int getSenderTotalGifts() { return senderTotalGifts; }
    public boolean isAnonymous() { return gifter == null; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
