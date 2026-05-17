package com.p0wders.sktwitch.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.Event;

@SuppressWarnings("unused")
public class UserStatus extends Condition {

    static {
        Skript.registerCondition(UserStatus.class,
                "%twitchuser% is [a] (0¦subscriber|1¦moderator|2¦broadcaster|3¦vip|4¦turbo|5¦founder)",
                "%twitchuser% is(n't| not) [a] (0¦subscriber|1¦moderator|2¦broadcaster|3¦vip|4¦turbo|5¦founder)"
        );
    }

    private int mark;
    private Expression<TwitchUser> userExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.mark = parseResult.mark;
        this.userExpr = (Expression<TwitchUser>) exprs[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event e) {
        TwitchUser u = userExpr.getSingle(e);
        if (u == null) return false;
        boolean result = switch (mark) {
            case 0 -> u.isSubscriber();
            case 1 -> u.isModerator();
            case 2 -> u.isBroadcaster();
            case 3 -> u.isVip();
            case 4 -> u.isTurbo();
            case 5 -> u.isFounder();
            default -> false;
        };
        return isNegated() != result;
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch user status"; }
}
