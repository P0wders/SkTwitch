package com.p0wders.sktwitch.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.SkTwitch;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only introspection of the BridgeManager state.
 *
 * Patterns:
 *   0 - all [registered] twitch bridges                 (list of bridge names)
 *   1 - all connected twitch bridges                    (list — bridges with ≥1 live socket)
 *   2 - [all] [joined] twitch channels of bridge %string% (list of channel names with #)
 */
@SuppressWarnings({"unused", "removal"})
public class BridgeIntrospection extends SimpleExpression<String> {

    static {
        Skript.registerExpression(BridgeIntrospection.class, String.class, ExpressionType.COMBINED,
                "all [registered] twitch bridges",
                "all connected twitch bridges",
                "[all] [joined] twitch channels of bridge %string%"
        );
    }

    private int pattern;
    @Nullable private Expression<String> bridgeExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (matchedPattern == 2) {
            this.bridgeExpr = (Expression<String>) expressions[0];
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        var mgr = SkTwitch.getInstance().getBridgeManager();
        return switch (pattern) {
            case 1 -> mgr.connectedBridges().toArray(new String[0]);
            case 2 -> {
                if (bridgeExpr == null) yield new String[0];
                String name = bridgeExpr.getSingle(e);
                if (name == null) yield new String[0];
                yield mgr.channelsOfBridge(name).toArray(new String[0]);
            }
            default -> mgr.registeredBridges().toArray(new String[0]);
        };
    }

    @Override public boolean isSingle() { return false; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch bridge introspection"; }
}