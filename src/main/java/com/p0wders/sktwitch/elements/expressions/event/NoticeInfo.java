package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchNoticeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class NoticeInfo extends SimpleExpression<String> {

    static {
        Skript.registerExpression(NoticeInfo.class, String.class, ExpressionType.SIMPLE,
                "[the] twitch notice id",        // 0 -> msg-id (e.g. msg_banned)
                "[the] twitch notice message"    // 1 -> body text
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchNoticeEvent.class)) {
            Skript.error("notice info can only be used in a twitch notice event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (!(e instanceof TwitchNoticeEvent ev)) return null;
        return new String[]{ pattern == 1 ? ev.getMessage() : ev.getNoticeId() };
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch notice info"; }
}