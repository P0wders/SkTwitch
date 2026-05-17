package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchBanEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class BanInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(BanInfo.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch banned user[name]",   // 0 → String
                "[the] twitch banned user id",      // 1 → String
                "[the] twitch ban duration"         // 2 → Number (seconds; 0 = permanent)
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchBanEvent.class)) {
            Skript.error("ban info can only be used in a twitch ban/timeout event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (!(e instanceof TwitchBanEvent ev)) return null;
        return switch (pattern) {
            case 0 -> new Object[]{ ev.getTargetLogin() };
            case 1 -> new Object[]{ ev.getTargetUserId() };
            case 2 -> new Object[]{ ev.getDurationSeconds() };
            default -> null;
        };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<?> getReturnType() {
        return pattern == 2 ? Number.class : String.class;
    }
    @Override public String toString(Event e, boolean debug) { return "twitch ban info"; }
}