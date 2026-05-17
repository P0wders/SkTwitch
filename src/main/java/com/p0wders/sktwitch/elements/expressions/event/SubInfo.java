package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchSubscribeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class SubInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(SubInfo.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch sub tier",              // 0 → String
                "[the] twitch [sub] cumulative months",  // 1 → Number
                "[the] twitch [sub] streak months",   // 2 → Number
                "[the] twitch sub message"            // 3 → String (nullable)
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchSubscribeEvent.class)) {
            Skript.error("sub info can only be used in a twitch sub/resub event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (!(e instanceof TwitchSubscribeEvent ev)) return null;
        return switch (pattern) {
            case 0 -> new Object[]{ ev.getTier() };
            case 1 -> new Object[]{ ev.getCumulativeMonths() };
            case 2 -> new Object[]{ ev.getStreakMonths() };
            case 3 -> ev.getMessage() == null ? null : new Object[]{ ev.getMessage() };
            default -> null;
        };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<?> getReturnType() {
        return pattern == 1 || pattern == 2 ? Number.class : String.class;
    }
    @Override public String toString(Event e, boolean debug) { return "twitch sub info"; }
}