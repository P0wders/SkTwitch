package com.p0wders.sktwitch.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.SkTwitch;
import org.bukkit.event.Event;

/**
 * "bridge %string% is [currently] connected" — true if at least one of the
 * bridge's channels has a live WebSocket. A bridge with zero channels is
 * registered but never "connected".
 */
@Name("Bridge Is Connected")
@Description("Checks whether a Twitch bridge is currently connected. A bridge is connected when at least one of its channels has a live WebSocket connection.")
@Since("1.0.0")
@SuppressWarnings({"unused", "removal"})
public class BridgeIsConnected extends Condition {

    static {
        Skript.registerCondition(BridgeIsConnected.class,
                "[twitch] bridge %string% is [currently] connected",
                "[twitch] bridge %string% is(n't| not) [currently] connected"
        );
    }

    private Expression<String> nameExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.nameExpr = (Expression<String>) expressions[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event e) {
        String name = nameExpr.getSingle(e);
        if (name == null) return isNegated();
        boolean connected = SkTwitch.getInstance().getBridgeManager().isBridgeConnected(name);
        return isNegated() != connected;
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch bridge is connected"; }
}