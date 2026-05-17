package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchClearChatEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public TwitchClearChatEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName) {
        super(channel, bridgeName);
    }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}