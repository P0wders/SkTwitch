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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ExprRoles extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprRoles.class, String.class, ExpressionType.SIMPLE,
                "[the] twitch roles");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("twitch roles can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return null;
        // Ordered by conventional priority: broadcaster > moderator > vip > subscriber
        List<String> roles = new ArrayList<>();
        if (tme.isBroadcaster()) roles.add("broadcaster");
        if (tme.isModerator()) roles.add("moderator");
        if (tme.isVip()) roles.add("vip");
        if (tme.isSubscriber()) roles.add("subscriber");
        return roles.toArray(new String[0]);
    }

    @Override public boolean isSingle() { return false; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch roles"; }
}