package com.p0wders.sktwitch.elements.expressions.message;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchMessageEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class MessageId extends SimpleExpression<String> {

    static {
        Skript.registerExpression(MessageId.class, String.class, ExpressionType.SIMPLE,
                "[the] twitch message id");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("twitch message id can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return null;
        return new String[]{ tme.getMessageId() };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch message id"; }
}