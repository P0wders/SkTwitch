package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchBanEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String targetLogin;
    private final String targetUserId;
    private final int durationSeconds; // 0 = permanent ban

    public TwitchBanEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                          @NotNull String targetLogin, @NotNull String targetUserId,
                          int durationSeconds) {
        super(channel, bridgeName);
        this.targetLogin = targetLogin;
        this.targetUserId = targetUserId;
        this.durationSeconds = durationSeconds;
    }

    public @NotNull String getTargetLogin() { return targetLogin; }
    public @NotNull String getTargetUserId() { return targetUserId; }
    public int getDurationSeconds() { return durationSeconds; }
    public boolean isPermanent() { return durationSeconds == 0; }
    public boolean isTimeout() { return durationSeconds > 0; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}