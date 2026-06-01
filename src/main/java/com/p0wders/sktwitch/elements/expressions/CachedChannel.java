package com.p0wders.sktwitch.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.SkTwitch;
import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up a TwitchChannel by name. Returns null if no event has been seen
 * yet for that channel (room-id won't be known until ROOMSTATE arrives).
 *
 * Patterns:
 *   0 - twitch channel [named] %string% [(in|from|of) bridge %string%]
 *   1 - all (cached|known) twitch channels [(in|from|of) bridge %string%]
 */
@SuppressWarnings({"unused", "removal"})
public class CachedChannel extends SimpleExpression<TwitchChannel> {

    static {
        Skript.registerExpression(CachedChannel.class, TwitchChannel.class, ExpressionType.COMBINED,
                "[the] twitch channel [named] %string% [(in|from|of) bridge %-string%]",
                "[all] (cached|known) twitch channels [(in|from|of) bridge %-string%]"
        );
    }

    private int pattern;
    @Nullable private Expression<String> nameExpr;
    @Nullable private Expression<String> bridgeExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (matchedPattern == 0) {
            this.nameExpr = (Expression<String>) expressions[0];
            this.bridgeExpr = (Expression<String>) expressions[1];
        } else {
            this.bridgeExpr = (Expression<String>) expressions[0];
        }
        return true;
    }

    @Override
    protected TwitchChannel @Nullable [] get(Event e) {
        var mgr = SkTwitch.getInstance().getBridgeManager();
        String bridge = bridgeExpr != null ? bridgeExpr.getSingle(e) : null;

        if (pattern == 0) {
            String name = nameExpr.getSingle(e);
            if (name == null) return null;
            TwitchChannel c = mgr.lookupChannel(name, bridge);
            return c == null ? null : new TwitchChannel[]{ c };
        }

        return mgr.allCachedChannels(bridge).toArray(new TwitchChannel[0]);
    }

    @Override public boolean isSingle() { return pattern == 0; }
    @Override public Class<? extends TwitchChannel> getReturnType() { return TwitchChannel.class; }
    @Override public String toString(Event e, boolean debug) { return "cached twitch channel"; }
}