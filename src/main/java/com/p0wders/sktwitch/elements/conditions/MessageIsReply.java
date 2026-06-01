package com.p0wders.sktwitch.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchMessageEvent;
import org.bukkit.event.Event;

@Name("Message Is Reply")
@Description("Checks whether the Twitch message is a reply to another message. Can only be used in a Twitch message event.")
@Since("1.0.0")
@SuppressWarnings("unused")
public class MessageIsReply extends Condition {

    static {
        Skript.registerCondition(MessageIsReply.class,
                "[the] [twitch] message is a reply",
                "[the] [twitch] message is(n't| not) a reply"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setNegated(matchedPattern == 1);
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("'message is a reply' can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return false;
        return isNegated() != tme.isReply();
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch message is a reply"; }
}