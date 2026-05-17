package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchAnnouncementEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class AnnouncementInfo extends SimpleExpression<String> {

    static {
        Skript.registerExpression(AnnouncementInfo.class, String.class, ExpressionType.SIMPLE,
                "[the] twitch announcement message",   // 0
                "[the] twitch announcement color"      // 1
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchAnnouncementEvent.class)) {
            Skript.error("announcement info can only be used in a twitch announcement event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (!(e instanceof TwitchAnnouncementEvent ev)) return null;
        return new String[]{ pattern == 1 ? ev.getColor() : ev.getMessage() };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch announcement info"; }
}