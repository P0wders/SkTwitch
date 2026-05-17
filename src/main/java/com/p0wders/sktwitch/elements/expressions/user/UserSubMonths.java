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

@SuppressWarnings("unused")
public class UserSubMonths extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(UserSubMonths.class, Number.class, ExpressionType.PROPERTY,
                "twitch sub[scriber] months of %twitchuser%");
    }

    private Expression<TwitchUser> userExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.userExpr = (Expression<TwitchUser>) exprs[0];
        return true;
    }

    @Override
    protected Number @Nullable [] get(Event e) {
        TwitchUser u = userExpr.getSingle(e);
        if (u == null) return null;
        return new Number[]{ u.getSubMonths() };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends Number> getReturnType() { return Number.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch sub months"; }
}