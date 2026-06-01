package com.p0wders.sktwitch.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchNoticeEvent;
import org.bukkit.event.Event;

@Name("Notice Is Error")
@Description("Checks whether the Twitch notice is an error. Can only be used in a Twitch notice event.")
@Since("1.0.0")
@SuppressWarnings("unused")
public class NoticeIsError extends Condition {

    static {
        Skript.registerCondition(NoticeIsError.class,
                "[the] [twitch] notice is [an] error",
                "[the] [twitch] notice is(n't| not) [an] error"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setNegated(matchedPattern == 1);
        if (!getParser().isCurrentEvent(TwitchNoticeEvent.class)) {
            Skript.error("'notice is an error' can only be used in a twitch notice event");
            return false;
        }
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchNoticeEvent ev)) return false;
        return isNegated() != ev.isError();
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch notice is an error"; }
}