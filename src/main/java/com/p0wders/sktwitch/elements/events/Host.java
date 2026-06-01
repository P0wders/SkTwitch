package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchHostEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class Host extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Host", Host.class, TwitchHostEvent.class,
                "twitch host[target]",
                "twitch host (start|begin)",
                "twitch host (stop|end)");
    }

    private int pattern;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchHostEvent ev)) return false;
        return switch (pattern) {
            case 1 -> !ev.isHostStop();
            case 2 -> ev.isHostStop();
            default -> true;
        };
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch host event"; }
}