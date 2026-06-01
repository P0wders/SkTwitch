package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchNoticeEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class Notice extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Notice", Notice.class, TwitchNoticeEvent.class,
                "twitch notice",
                "twitch (server|irc) notice",
                "twitch notice error",
                "twitch (server|irc) notice error");
    }

    private int pattern;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchNoticeEvent ev)) return false;
        // patterns 2 & 3 fire only on error notices
        return (pattern < 2) || ev.isError();
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch notice event"; }
}