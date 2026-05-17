package com.p0wders.sktwitch.elements.expressions.channel;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ChannelProperty extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ChannelProperty.class, String.class, ExpressionType.PROPERTY,
                "twitch channel name of %twitchchannel%",
                "twitch channel login of %twitchchannel%",
                "twitch room id of %twitchchannel%"
        );
    }

    private int pattern;
    private Expression<TwitchChannel> channelExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        this.channelExpr = (Expression<TwitchChannel>) exprs[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        TwitchChannel c = channelExpr.getSingle(e);
        if (c == null) return null;
        return new String[]{ switch (pattern) {
            case 1 -> c.getNameWithoutHash();
            case 2 -> c.getRoomId();
            default -> c.getName();
        }};
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch channel property"; }
}