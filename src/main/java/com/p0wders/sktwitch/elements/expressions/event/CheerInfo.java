package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchCheerEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CheerInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(CheerInfo.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch bits [amount]",   // 0 → Number
                "[the] twitch cheer message"    // 1 → String
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchCheerEvent.class)) {
            Skript.error("cheer info can only be used in a twitch cheer event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (!(e instanceof TwitchCheerEvent ev)) return null;
        return switch (pattern) {
            case 0 -> new Object[]{ ev.getBits() };
            case 1 -> new Object[]{ ev.getMessage() };
            default -> null;
        };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<?> getReturnType() {
        return pattern == 0 ? Number.class : String.class;
    }
    @Override public String toString(Event e, boolean debug) { return "twitch cheer info"; }
}