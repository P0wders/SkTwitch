package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/** Base for all SkTwitch events. Carries channel + bridge name. */
public abstract class TwitchBaseEvent extends Event {
    private final TwitchChannel channel;
    private final String bridgeName;

    protected TwitchBaseEvent(@NotNull TwitchChannel channel, @NotNull String bridgeName) {
        super(false);
        this.channel = channel;
        this.bridgeName = bridgeName;
    }

    public @NotNull TwitchChannel getChannel() { return channel; }
    public @NotNull String getBridgeName() { return bridgeName; }
}