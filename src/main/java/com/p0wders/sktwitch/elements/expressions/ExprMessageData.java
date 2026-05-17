package com.p0wders.sktwitch.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.events.TwitchMessageEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ExprMessageData extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprMessageData.class, String.class, ExpressionType.SIMPLE,
                "[the] twitch user[name]",
                "[the] twitch display name",
                "[the] twitch message",
                "[the] twitch channel",
                "[the] twitch color",
                "[the] twitch bridge [name]",
                "[the] twitch user id",
                "[the] twitch message id",
                "[the] twitch room id",
                "[the] twitch badges"
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
            Skript.error("Twitch expressions can only be used in a twitch message event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(Event e) {
        if (!(e instanceof TwitchMessageEvent tme)) return null;
        return new String[]{ switch (pattern) {
            case 1 -> tme.getDisplayName();
            case 2 -> tme.getMessage();
            case 3 -> tme.getChannel();
            case 4 -> tme.getColor();
            case 5 -> tme.getBridgeName();
            case 6 -> tme.getUserId();
            case 7 -> tme.getMessageId();
            case 8 -> tme.getRoomId();
            case 9 -> tme.getBadges();
            default -> tme.getUsername();
        }};
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends String> getReturnType() { return String.class; }
    @Override public String toString(Event e, boolean debug) { return "twitch message data"; }
}