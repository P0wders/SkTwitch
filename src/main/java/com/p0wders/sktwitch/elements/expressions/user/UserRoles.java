package com.p0wders.sktwitch.elements.expressions.user;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class UserRoles extends SimpleExpression<String> {

    static {
        Skript.registerExpression(UserRoles.class, String.class, ExpressionType.PROPERTY,
                "twitch roles of %twitchuser%");
    }

    private Expression<TwitchUser> userExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.userExpr = (Expression<TwitchUser>) exprs[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        TwitchUser u = userExpr.getSingle(e);
        if (u == null) return null;
        List<String> roles = new ArrayList<>();
        if (u.isBroadcaster()) roles.add("broadcaster");
        if (u.isModerator()) roles.add("moderator");
        if (u.isVip()) roles.add("vip");
        if (u.isSubscriber()) roles.add("subscriber");
        return roles.toArray(new String[0]);
    }

    @Override public boolean isSingle() { return false; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch roles"; }
}