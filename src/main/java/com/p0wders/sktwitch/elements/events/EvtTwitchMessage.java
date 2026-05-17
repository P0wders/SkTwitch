package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.events.TwitchMessageEvent;
import org.bukkit.event.Event;

@SuppressWarnings("unused")
public class EvtTwitchMessage extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Message", EvtTwitchMessage.class, TwitchMessageEvent.class,
                "twitch message",
                "twitch message in [channel] %string%",
                "twitch message from bridge %string%");
    }

    private int pattern;
    private Literal<String> filter;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        this.pattern = matchedPattern;
        this.filter = matchedPattern == 0 ? null : (Literal<String>) args[0];
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return false;
        if (filter == null) return true;
        String value = filter.getSingle();
        if (value == null) return false;

        return switch (pattern) {
            case 1 -> tme.getChannel().replace("#", "").equalsIgnoreCase(value.replace("#", ""));
            case 2 -> tme.getBridgeName().equalsIgnoreCase(value);
            default -> true;
        };
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch message"; }
}