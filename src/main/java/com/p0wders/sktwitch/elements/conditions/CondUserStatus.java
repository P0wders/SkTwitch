package com.p0wders.sktwitch.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.events.TwitchMessageEvent;
import org.bukkit.event.Event;

@SuppressWarnings("unused")
public class CondUserStatus extends Condition {

    static {
        Skript.registerCondition(CondUserStatus.class,
                "[the] twitch user is [a] (0¦subscriber|1¦moderator|2¦broadcaster|3¦vip|4¦turbo)",
                "[the] twitch user is(n't| not) [a] (0¦subscriber|1¦moderator|2¦broadcaster|3¦vip|4¦turbo)",
                "[the] twitch message is [the] (5¦first message) [of [the] user]",
                "[the] twitch message is(n't| not) [the] (5¦first message) [of [the] user]"
        );
    }

    private int mark;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.mark = parseResult.mark;
        setNegated(matchedPattern == 1 || matchedPattern == 3);
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("Twitch conditions can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return false;
        boolean result = switch (mark) {
            case 0 -> tme.isSubscriber();
            case 1 -> tme.isModerator();
            case 2 -> tme.isBroadcaster();
            case 3 -> tme.isVip();
            case 4 -> tme.isTurbo();
            case 5 -> tme.isFirstMessage();
            default -> false;
        };
        return isNegated() != result;
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch user status check"; }
}