package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchSubscribeEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class Subscribe extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Sub", Subscribe.class, TwitchSubscribeEvent.class,
                "twitch sub[scription]",
                "twitch resub[scription]",
                "twitch (sub[scription]|resub[scription])");
    }

    private int pattern;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchSubscribeEvent ev)) return false;
        return switch (pattern) {
            case 0 -> !ev.isResub();   // only first-time
            case 1 -> ev.isResub();    // only resubs
            default -> true;            // either
        };
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch sub event"; }
}