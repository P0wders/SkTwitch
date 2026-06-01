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

/**
 * Boolean flag conditions on {@link TwitchMessageEvent}, dispatched via the
 * pattern's parse mark (same shape as {@code UserStatus}). Marks:
 *   0 - first message
 *   1 - returning chatter
 *   2 - has custom reward (channel-points redemption with required message)
 *   3 - is hype chat (paid pinned)
 *   4 - is shared chat (cross-channel duplicated message)
 */
@Name("Message Flags")
@Description({"Checks boolean flags on a Twitch message: first message, returning chatter, custom reward (channel-points redemption), hype chat or shared chat.",
        "Can only be used in a Twitch message event."})
@Since("1.0.0")
@SuppressWarnings({"unused", "removal"})
public class MessageFlags extends Condition {

    static {
        Skript.registerCondition(MessageFlags.class,
                "[the] [twitch] message is [a] (0¦first message|1¦returning chatter['s message]|2¦(custom reward|reward redemption|channel point[s] redemption)|3¦hype chat|4¦shared chat [message])",
                "[the] [twitch] message is(n't| not) [a] (0¦first message|1¦returning chatter['s message]|2¦(custom reward|reward redemption|channel point[s] redemption)|3¦hype chat|4¦shared chat [message])"
        );
    }

    private int mark;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.mark = parseResult.mark;
        setNegated(matchedPattern == 1);
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("this condition can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof TwitchMessageEvent ev)) return false;
        boolean result = switch (mark) {
            case 0 -> ev.isFirstMessage();
            case 1 -> ev.isReturningChatter();
            case 2 -> ev.hasCustomReward();
            case 3 -> ev.isHypeChat();
            case 4 -> ev.isSharedChat();
            default -> false;
        };
        return isNegated() != result;
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch message flag check"; }
}