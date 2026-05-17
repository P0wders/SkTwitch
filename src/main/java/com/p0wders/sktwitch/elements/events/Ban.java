package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchBanEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class Ban extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Ban/Timeout", Ban.class, TwitchBanEvent.class,
                "twitch ban",
                "twitch timeout",
                "twitch (ban|timeout)");
    }

    private int pattern;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchBanEvent ev)) return false;
        return switch (pattern) {
            case 0 -> ev.isPermanent();   // ban
            case 1 -> ev.isTimeout();     // timeout
            default -> true;
        };
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch ban/timeout event"; }
}