package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TwitchSubscribeEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final TwitchUser user;
    private final String tier;            // "Prime", "1000", "2000", "3000"
    private final String subPlanName;     // human-readable channel-defined plan name
    private final int cumulativeMonths;
    private final int streakMonths;       // 0 if user didn't share
    private final int multimonthDuration; // length of the current multimonth purchase (0 if N/A)
    private final int multimonthTenure;   // months elapsed in the current multimonth (0 if N/A)
    private final boolean resub;
    private final @Nullable String message;

    public TwitchSubscribeEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                                @NotNull TwitchUser user, @NotNull String tier,
                                @NotNull String subPlanName,
                                int cumulativeMonths, int streakMonths,
                                int multimonthDuration, int multimonthTenure,
                                boolean resub, @Nullable String message) {
        super(channel, bridgeName);
        this.user = user;
        this.tier = tier;
        this.subPlanName = subPlanName;
        this.cumulativeMonths = cumulativeMonths;
        this.streakMonths = streakMonths;
        this.multimonthDuration = multimonthDuration;
        this.multimonthTenure = multimonthTenure;
        this.resub = resub;
        this.message = message;
    }

    public @NotNull TwitchUser getUser() { return user; }
    public @NotNull String getTier() { return tier; }
    public @NotNull String getSubPlanName() { return subPlanName; }
    public int getCumulativeMonths() { return cumulativeMonths; }
    public int getStreakMonths() { return streakMonths; }
    public int getMultimonthDuration() { return multimonthDuration; }
    public int getMultimonthTenure() { return multimonthTenure; }
    public boolean isResub() { return resub; }
    public @Nullable String getMessage() { return message; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
