package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchCheerEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class Cheer extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Cheer", Cheer.class, TwitchCheerEvent.class,
                "twitch cheer",
                "twitch bits");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) { return true; }

    @Override public boolean check(Event e) { return e instanceof TwitchCheerEvent; }
    @Override public String toString(Event e, boolean debug) { return "twitch cheer event"; }
}