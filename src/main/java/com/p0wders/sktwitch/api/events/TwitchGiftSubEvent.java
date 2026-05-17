package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TwitchGiftSubEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final @Nullable TwitchUser gifter;     // null if anonymous gift
    private final TwitchUser recipient;
    private final String tier;
    private final String subPlanName;              // human-readable plan name
    private final int recipientTotalMonths;        // msg-param-months (recipient cumulative)
    private final int giftDurationMonths;          // msg-param-gift-months (1/3/6/12)
    private final String originId;                 // links sub-gifts back to a parent community gift
    private final int senderTotalGifts;            // msg-param-sender-count (lifetime in channel)

    public TwitchGiftSubEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                              @Nullable TwitchUser gifter, @NotNull TwitchUser recipient,
                              @NotNull String tier, @NotNull String subPlanName,
                              int recipientTotalMonths, int giftDurationMonths,
                              @NotNull String originId, int senderTotalGifts) {
        super(channel, bridgeName);
        this.gifter = gifter;
        this.recipient = recipient;
        this.tier = tier;
        this.subPlanName = subPlanName;
        this.recipientTotalMonths = recipientTotalMonths;
        this.giftDurationMonths = giftDurationMonths;
        this.originId = originId;
        this.senderTotalGifts = senderTotalGifts;
    }

    public @Nullable TwitchUser getGifter() { return gifter; }
    public @NotNull TwitchUser getRecipient() { return recipient; }
    public @NotNull String getTier() { return tier; }
    public @NotNull String getSubPlanName() { return subPlanName; }

    /** Recipient's total months after this gift (msg-param-months). */
    public int getRecipientTotalMonths() { return recipientTotalMonths; }

    /** Months purchased in this single gift, typically 1/3/6/12 (msg-param-gift-months). */
    public int getGiftDurationMonths() { return giftDurationMonths; }

    /** Alias kept for backwards readability — same as {@link #getRecipientTotalMonths()}. */
    public int getMonths() { return recipientTotalMonths; }

    public @NotNull String getOriginId() { return originId; }
    public int getSenderTotalGifts() { return senderTotalGifts; }
    public boolean isAnonymous() { return gifter == null; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
