package com.p0wders.sktwitch.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.p0wders.sktwitch.api.events.TwitchClearChatEvent;
import org.bukkit.event.Event;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ClearChat extends SkriptEvent {

    static {
        Skript.registerEvent("Twitch Clear Chat", ClearChat.class, TwitchClearChatEvent.class,
                "twitch (clear chat|chat clear)");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) { return true; }

    @Override public boolean check(Event e) { return e instanceof TwitchClearChatEvent; }
    @Override public String toString(Event e, boolean debug) { return "twitch clear chat event"; }
}