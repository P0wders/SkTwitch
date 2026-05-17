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
public class ExprHighestRole extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprHighestRole.class, String.class, ExpressionType.SIMPLE,
                "[the] (highest|top) twitch role");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("twitch role can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return null;
        String highest;
        if (tme.isBroadcaster()) highest = "broadcaster";
        else if (tme.isModerator()) highest = "moderator";
        else if (tme.isVip()) highest = "vip";
        else if (tme.isSubscriber()) highest = "subscriber";
        else highest = "";
        return new String[]{ highest };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "highest twitch role"; }
}