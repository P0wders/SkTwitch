package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchMessageDeleteEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String targetLogin;
    private final String targetMessageId;
    private final String messageBody;

    public TwitchMessageDeleteEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName,
                                    @NotNull String targetLogin, @NotNull String targetMessageId,
                                    @NotNull String messageBody) {
        super(channel, bridgeName);
        this.targetLogin = targetLogin;
        this.targetMessageId = targetMessageId;
        this.messageBody = messageBody;
    }

    public @NotNull String getTargetLogin() { return targetLogin; }
    public @NotNull String getTargetMessageId() { return targetMessageId; }
    public @NotNull String getMessageBody() { return messageBody; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}