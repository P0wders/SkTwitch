package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchMessageDeleteEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class MessageDelete extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Message Delete", MessageDelete.class, TwitchMessageDeleteEvent.class,
                "twitch message delete[d]",
                "twitch deleted message");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) { return true; }

    @Override public boolean check(Event e) { return e instanceof TwitchMessageDeleteEvent; }
    @Override public String toString(Event e, boolean debug) { return "twitch message delete event"; }
}