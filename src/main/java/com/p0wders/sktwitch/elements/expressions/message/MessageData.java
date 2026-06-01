package com.p0wders.sktwitch.elements.expressions.message;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.api.events.TwitchMessageEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes metadata that {@link TwitchMessageEvent} already carries but has had
 * no Skript binding for: the channel-points custom reward id, the server-side
 * timestamp (tmi-sent-ts), and the raw AutoMod flags tag.
 *
 * Pattern indices:
 *   0 - twitch custom reward id    (String, "" if none)
 *   1 - twitch message timestamp   (Number, epoch millis; 0 if absent)
 *   2 - twitch automod flags       (String, "" if absent)
 */
@SuppressWarnings({"unused", "removal"})
public class MessageData extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(MessageData.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch (custom reward id|reward id)",
                "[the] twitch message (timestamp|sent time)",
                "[the] twitch (automod|auto[ ]mod) flags"
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("twitch message metadata can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageEvent ev)) return null;
        return switch (pattern) {
            case 1 -> new Object[]{ ev.getTimestamp() };
            case 2 -> new Object[]{ ev.getAutoModFlags() };
            default -> new Object[]{ ev.getCustomRewardId() };
        };
    }

    @Override public boolean isSingle() { return true; }

    @Override
    public Class<?> getReturnType() {
        return pattern == 1 ? Number.class : String.class;
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch message metadata"; }
}