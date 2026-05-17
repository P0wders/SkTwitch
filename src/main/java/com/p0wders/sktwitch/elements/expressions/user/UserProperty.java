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
public class UserProperty extends SimpleExpression<String> {

    static {
        Skript.registerExpression(UserProperty.class, String.class, ExpressionType.PROPERTY,
                "twitch display name of %twitchuser%",
                "twitch (username|login) of %twitchuser%",
                "twitch user[ ]id of %twitchuser%",
                "twitch color of %twitchuser%",
                "twitch badges of %twitchuser%"
        );
    }

    private int pattern;
    private Expression<TwitchUser> userExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        this.userExpr = (Expression<TwitchUser>) exprs[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        TwitchUser u = userExpr.getSingle(e);
        if (u == null) return null;
        return new String[]{ switch (pattern) {
            case 1 -> u.getUsername();
            case 2 -> u.getUserId();
            case 3 -> u.getColor();
            case 4 -> u.getBadges();
            default -> u.getDisplayName();
        }};
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch user property"; }
}