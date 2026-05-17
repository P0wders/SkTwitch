package com.p0wders.sktwitch.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchBaseEvent;
import com.p0wders.sktwitch.api.events.TwitchMessageEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class StructBridgeName extends SimpleExpression<String> {

    static {
        Skript.registerExpression(StructBridgeName.class, String.class, ExpressionType.SIMPLE,
                "[the] [twitch] bridge [name]");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)
                && !getParser().isCurrentEvent(TwitchBaseEvent.class)) {
            Skript.error("bridge name can only be used in a twitch event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (e instanceof TwitchMessageEvent tme) return new String[]{ tme.getBridgeName() };
        if (e instanceof TwitchBaseEvent tbe) return new String[]{ tbe.getBridgeName() };
        return null;
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch bridge name"; }
}
