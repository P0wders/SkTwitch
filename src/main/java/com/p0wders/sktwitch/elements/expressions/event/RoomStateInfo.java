package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchRoomStateEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class RoomStateInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(RoomStateInfo.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch room state mode",   // 0 → String
                "[the] twitch room state value"   // 1 → Number
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchRoomStateEvent.class)) {
            Skript.error("room state info can only be used in a twitch room state event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (!(e instanceof TwitchRoomStateEvent ev)) return null;
        return switch (pattern) {
            case 0 -> new Object[]{ ev.getMode() };
            case 1 -> new Object[]{ ev.getValue() };
            default -> null;
        };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<?> getReturnType() {
        return pattern == 1 ? Number.class : String.class;
    }
    @Override public String toString(Event e, boolean debug) { return "twitch room state info"; }
}