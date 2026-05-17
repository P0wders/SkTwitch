package com.p0wders.sktwitch.elements.expressions.message;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.p0wders.sktwitch.TwitchUser;
import com.p0wders.sktwitch.api.events.TwitchMessageEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ReplyParent {

    // user
    public static class User extends SimpleExpression<TwitchUser> {
        static {
            Skript.registerExpression(User.class, TwitchUser.class, ExpressionType.SIMPLE,
                    "[the] reply parent [twitch ]user");
        }
        @Override public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
                Skript.error("reply parent user can only be used in a twitch message event");
                return false;
            }
            return true;
        }
        @Override protected TwitchUser @Nullable [] get(Event e) {
            if (!(e instanceof TwitchMessageEvent tme) || !tme.isReply()) return null;
            TwitchUser u = tme.getReplyParentUser();
            return u == null ? null : new TwitchUser[]{ u };
        }
        @Override public boolean isSingle() { return true; }
        @Override public Class<? extends TwitchUser> getReturnType() { return TwitchUser.class; }
        @Override public String toString(Event e, boolean debug) { return "reply parent user"; }
    }

    // message body
    public static class Message extends SimpleExpression<String> {
        static {
            Skript.registerExpression(Message.class, String.class, ExpressionType.SIMPLE,
                    "[the] reply parent [twitch ]message");
        }
        @Override public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
                Skript.error("reply parent message can only be used in a twitch message event");
                return false;
            }
            return true;
        }
        @Override protected String @Nullable [] get(Event e) {
            if (!(e instanceof TwitchMessageEvent tme) || !tme.isReply()) return null;
            return new String[]{ tme.getReplyParentMessage() };
        }
        @Override public boolean isSingle() { return true; }
        @Override public Class<? extends String> getReturnType() { return String.class; }
        @Override public String toString(Event e, boolean debug) { return "reply parent message"; }
    }

    // message ID
    public static class MessageId extends SimpleExpression<String> {
        static {
            Skript.registerExpression(MessageId.class, String.class, ExpressionType.SIMPLE,
                    "[the] reply parent [twitch ]message id");
        }
        @Override public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            if (!getParser().isCurrentEvent(TwitchMessageEvent.class)) {
                Skript.error("reply parent message id can only be used in a twitch message event");
                return false;
            }
            return true;
        }
        @Override protected String @Nullable [] get(Event e) {
            if (!(e instanceof TwitchMessageEvent tme) || !tme.isReply()) return null;
            return new String[]{ tme.getReplyParentMessageId() };
        }
        @Override public boolean isSingle() { return true; }
        @Override public Class<? extends String> getReturnType() { return String.class; }
        @Override public String toString(Event e, boolean debug) { return "reply parent message id"; }
    }
}