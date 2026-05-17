package com.p0wders.sktwitch.elements.expressions.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.TwitchUser;
import com.p0wders.sktwitch.api.events.TwitchGiftSubEvent;
import com.p0wders.sktwitch.api.events.TwitchMassGiftSubEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class GiftInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(GiftInfo.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch gifter",            // 0 → TwitchUser (nullable for anon)
                "[the] twitch gift recipient",    // 1 → TwitchUser
                "[the] twitch gift tier",         // 2 → String
                "[the] twitch gift months",       // 3 → Number
                "[the] twitch gift count"         // 4 → Number (mass gift)
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        boolean inGift = getParser().isCurrentEvent(TwitchGiftSubEvent.class);
        boolean inMass = getParser().isCurrentEvent(TwitchMassGiftSubEvent.class);
        if (!inGift && !inMass) {
            Skript.error("gift info can only be used in a twitch gift sub event");
            return false;
        }
        if (pattern == 1 || pattern == 3) {
            if (!inGift) {
                Skript.error("recipient/months only available in single gift sub event");
                return false;
            }
        }
        if (pattern == 4 && !inMass) {
            Skript.error("gift count only available in mass gift sub event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (e instanceof TwitchGiftSubEvent ev) {
            return switch (pattern) {
                case 0 -> ev.getGifter() == null ? null : new Object[]{ ev.getGifter() };
                case 1 -> new Object[]{ ev.getRecipient() };
                case 2 -> new Object[]{ ev.getTier() };
                case 3 -> new Object[]{ ev.getMonths() };
                default -> null;
            };
        }
        if (e instanceof TwitchMassGiftSubEvent ev) {
            return switch (pattern) {
                case 0 -> ev.getGifter() == null ? null : new Object[]{ ev.getGifter() };
                case 2 -> new Object[]{ ev.getTier() };
                case 4 -> new Object[]{ ev.getCount() };
                default -> null;
            };
        }
        return null;
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<?> getReturnType() {
        return switch (pattern) {
            case 0, 1 -> TwitchUser.class;
            case 3, 4 -> Number.class;
            default -> String.class;
        };
    }
    @Override public String toString(Event e, boolean debug) { return "twitch gift info"; }
}