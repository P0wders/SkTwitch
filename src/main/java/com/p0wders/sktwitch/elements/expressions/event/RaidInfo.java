package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.TwitchUser;
import com.p0wders.sktwitch.api.events.TwitchRaidEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class RaidInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(RaidInfo.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch raider",        // 0 → TwitchUser
                "[the] twitch raid viewers"   // 1 → Number
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchRaidEvent.class)) {
            Skript.error("raid info can only be used in a twitch raid event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (!(e instanceof TwitchRaidEvent ev)) return null;
        return switch (pattern) {
            case 0 -> new Object[]{ ev.getRaider() };
            case 1 -> new Object[]{ ev.getViewerCount() };
            default -> null;
        };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<?> getReturnType() {
        return pattern == 0 ? TwitchUser.class : Number.class;
    }
    @Override public String toString(Event e, boolean debug) { return "twitch raid info"; }
}