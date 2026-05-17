package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchMassGiftSubEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class MassGiftSub extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Mass Gift Sub", MassGiftSub.class, TwitchMassGiftSubEvent.class,
                "twitch mass gift[ed] sub[scription][s]");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) { return true; }

    @Override public boolean check(Event e) { return e instanceof TwitchMassGiftSubEvent; }
    @Override public String toString(Event e, boolean debug) { return "twitch mass gift sub event"; }
}