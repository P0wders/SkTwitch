package com.p0wders.sktwitch.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.events.TwitchMessageEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ExprSubMonths extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprSubMonths.class, Number.class, ExpressionType.SIMPLE,
                "[the] twitch sub[scriber] months");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("Twitch expressions can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    protected Number @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return null;
        return new Number[]{ tme.getSubMonths() };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends Number> getReturnType() { return Number.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch sub months"; }
}