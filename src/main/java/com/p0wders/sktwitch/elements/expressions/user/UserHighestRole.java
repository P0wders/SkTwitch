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
public class UserHighestRole extends SimpleExpression<String> {

    static {
        Skript.registerExpression(UserHighestRole.class, String.class, ExpressionType.PROPERTY,
                "[the] highest twitch role of %twitchuser%",
                "[the] top twitch role of %twitchuser%");
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
        String role;
        if (u.isBroadcaster()) role = "broadcaster";
        else if (u.isModerator()) role = "moderator";
        else if (u.isVip()) role = "vip";
        else if (u.isSubscriber()) role = "subscriber";
        else role = "";
        return new String[]{ role };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "highest twitch role"; }
}