package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchUserPartEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class UserPart extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch User Part", UserPart.class, TwitchUserPartEvent.class,
                "twitch user (part[ed]|leave|left)",
                "twitch [chat] viewer (part[ed]|leave|left)");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(Event e) { return e instanceof TwitchUserPartEvent; }

    @Override
    public String toString(Event e, boolean debug) { return "twitch user part event"; }
}