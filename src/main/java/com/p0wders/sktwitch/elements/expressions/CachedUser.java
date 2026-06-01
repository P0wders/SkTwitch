package com.p0wders.sktwitch.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.SkTwitch;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up a previously-seen TwitchUser by login. Returns null if that user
 * has never appeared in any cached event (PRIVMSG, USERNOTICE, etc.) for the
 * given bridge. Useful for cross-event scripts: "if the player who linked to
 * Twitch user X is a subscriber, do Y".
 *
 * Patterns:
 *   0 - twitch user [named] %string% [(in|from|of) bridge %string%]
 *   1 - all (cached|known) twitch users [(in|from|of) bridge %string%]
 *
 * When the bridge argument is omitted, the lookup runs across all bridges and
 * returns the first hit (pattern 0) or the union (pattern 1).
 */
@SuppressWarnings({"unused", "removal"})
public class CachedUser extends SimpleExpression<TwitchUser> {

    static {
        Skript.registerExpression(CachedUser.class, TwitchUser.class, ExpressionType.COMBINED,
                "[the] twitch user [named] %string% [(in|from|of) bridge %-string%]",
                "[all] (cached|known) twitch users [(in|from|of) bridge %-string%]"
        );
    }

    private int pattern;
    @Nullable private Expression<String> loginExpr;
    @Nullable private Expression<String> bridgeExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (matchedPattern == 0) {
            this.loginExpr = (Expression<String>) expressions[0];
            this.bridgeExpr = (Expression<String>) expressions[1];
        } else {
            this.bridgeExpr = (Expression<String>) expressions[0];
        }
        return true;
    }

    @Override
    protected TwitchUser @Nullable [] get(Event e) {
        var mgr = SkTwitch.getInstance().getBridgeManager();
        String bridge = bridgeExpr != null ? bridgeExpr.getSingle(e) : null;

        if (pattern == 0) {
            String login = loginExpr.getSingle(e);
            if (login == null) return null;
            TwitchUser u = mgr.lookupUser(login, bridge);
            return u == null ? null : new TwitchUser[]{ u };
        }

        return mgr.allCachedUsers(bridge).toArray(new TwitchUser[0]);
    }

    @Override public boolean isSingle() { return pattern == 0; }
    @Override public Class<? extends TwitchUser> getReturnType() { return TwitchUser.class; }
    @Override public String toString(Event e, boolean debug) { return "cached twitch user"; }
}