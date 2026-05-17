package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchRoomStateEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class RoomState extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Room State Change", RoomState.class, TwitchRoomStateEvent.class,
                "twitch room state change",
                "twitch slow mode (toggle|change)",
                "twitch (sub[scriber]|sub[s]) only mode (toggle|change)",
                "twitch emote only mode (toggle|change)",
                "twitch follower[s] only mode (toggle|change)",
                "twitch unique mode (toggle|change)"
        );
    }

    private int pattern;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchRoomStateEvent ev)) return false;
        return switch (pattern) {
            case 1 -> ev.getMode().equals("slow");
            case 2 -> ev.getMode().equals("subs-only");
            case 3 -> ev.getMode().equals("emote-only");
            case 4 -> ev.getMode().equals("followers-only");
            case 5 -> ev.getMode().equals("r9k");
            default -> true;
        };
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch room state event"; }
}