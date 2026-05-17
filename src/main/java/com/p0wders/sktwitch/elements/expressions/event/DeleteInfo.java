package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchMessageDeleteEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class DeleteInfo extends SimpleExpression<String> {

    static {
        Skript.registerExpression(DeleteInfo.class, String.class, ExpressionType.SIMPLE,
                "[the] twitch deleted user[name]",    // 0
                "[the] twitch deleted message id",    // 1
                "[the] twitch deleted message"        // 2 (the body)
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchMessageDeleteEvent.class)) {
            Skript.error("delete info can only be used in a twitch message delete event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageDeleteEvent ev)) return null;
        return new String[]{ switch (pattern) {
            case 1 -> ev.getTargetMessageId();
            case 2 -> ev.getMessageBody();
            default -> ev.getTargetLogin();
        }};
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch delete info"; }
}