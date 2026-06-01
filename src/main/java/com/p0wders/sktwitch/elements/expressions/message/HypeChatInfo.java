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
 * Hype Chat (paid pinned message) data exposed on {@link TwitchMessageEvent}.
 * Hype Chat is the only paid-pinned-message system Twitch chat supports;
 * the user pays through Twitch to have their message pinned at the top of
 * chat for a duration determined by the {@code level}.
 *
 * Pattern indices:
 *   0 - twitch hype chat amount        (Number, smallest currency unit — e.g. cents)
 *   1 - twitch hype chat currency      (String, ISO 4217 — "USD", "EUR", "GBP", ...)
 *   2 - twitch hype chat level         (Number, 1-10 — tier purchased)
 *   3 - twitch hype chat real amount   (Number, decimal-adjusted human amount)
 *
 * The "real amount" pattern is a convenience: Twitch's {@code pinned-chat-paid-amount}
 * tag is always in the smallest unit of the currency (5.00 EUR arrives as 500). For
 * currencies with 2 decimal places this divides by 100; JPY, KRW and a few others
 * have 0 decimals and pass through unchanged.
 */
@SuppressWarnings({"unused", "removal"})
public class HypeChatInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(HypeChatInfo.class, Object.class, ExpressionType.SIMPLE,
                "[the] twitch hype chat amount",
                "[the] twitch hype chat currency",
                "[the] twitch hype chat level",
                "[the] twitch (real|decimal) hype chat amount"
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("hype chat info can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageEvent ev)) return null;
        return switch (pattern) {
            case 1 -> new Object[]{ ev.getHypeChatCurrency() };
            case 2 -> new Object[]{ ev.getHypeChatLevel() };
            case 3 -> new Object[]{ toRealAmount(ev.getHypeChatAmount(), ev.getHypeChatCurrency()) };
            default -> new Object[]{ ev.getHypeChatAmount() };
        };
    }

    /**
     * Converts a Twitch hype-chat raw amount (smallest unit) to the human-facing
     * decimal value, using the ISO 4217 minor-unit count for the given currency.
     * Defaults to 2 decimals when the currency is unknown or empty.
     */
    private static double toRealAmount(int rawAmount, String currency) {
        int decimals = minorUnitDecimals(currency);
        if (decimals == 0) return rawAmount;
        double divisor = Math.pow(10, decimals);
        return rawAmount / divisor;
    }

    /**
     * ISO 4217 minor unit table covering the currencies Twitch Hype Chat is
     * available in. Most are 2 decimals; CLP/JPY/KRW/HUF and a few others
     * have 0 decimals; some Middle-Eastern currencies have 3.
     */
    private static int minorUnitDecimals(String currency) {
        if (currency == null || currency.isEmpty()) return 2;
        return switch (currency) {
            // 0-decimal currencies (smallest unit IS the major unit)
            case "JPY", "KRW", "HUF", "CLP", "ISK", "UGX", "VND", "XAF", "XOF", "XPF" -> 0;
            // 3-decimal currencies (rare; not all are Hype-Chat enabled but cheap to list)
            case "BHD", "IQD", "JOD", "KWD", "LYD", "OMR", "TND" -> 3;
            // everything else uses 2 (USD, EUR, GBP, CAD, AUD, BRL, MXN, ...)
            default -> 2;
        };
    }

    @Override public boolean isSingle() { return true; }

    @Override
    public Class<?> getReturnType() {
        return pattern == 1 ? String.class : Number.class;
    }

    @Override
    public String toString(Event e, boolean debug) { return "twitch hype chat info"; }
}