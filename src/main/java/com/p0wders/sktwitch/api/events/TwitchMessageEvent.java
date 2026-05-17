package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import com.p0wders.sktwitch.TwitchUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TwitchMessageEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final TwitchUser user;
    private final TwitchChannel channel;
    private final String message;
    private final String messageId;
    private final String bridgeName;
    private final boolean firstMessage;

    private final boolean reply;
    private final @Nullable TwitchUser replyParentUser;
    private final String replyParentMessage;
    private final String replyParentMessageId;

    public TwitchMessageEvent(TwitchUser user, TwitchChannel channel, String message,
                              String messageId, String bridgeName, boolean firstMessage,
                              boolean reply, @Nullable TwitchUser replyParentUser,
                              String replyParentMessage, String replyParentMessageId) {
        super(false);
        this.user = user;
        this.channel = channel;
        this.message = message;
        this.messageId = messageId;
        this.bridgeName = bridgeName;
        this.firstMessage = firstMessage;
        this.reply = reply;
        this.replyParentUser = replyParentUser;
        this.replyParentMessage = replyParentMessage;
        this.replyParentMessageId = replyParentMessageId;
    }

    public TwitchUser getUser() { return user; }
    public TwitchChannel getChannel() { return channel; }
    public String getMessage() { return message; }
    public String getMessageId() { return messageId; }
    public String getBridgeName() { return bridgeName; }
    public boolean isFirstMessage() { return firstMessage; }
    public boolean isReply() { return reply; }
    public @Nullable TwitchUser getReplyParentUser() { return replyParentUser; }
    public String getReplyParentMessage() { return replyParentMessage; }
    public String getReplyParentMessageId() { return replyParentMessageId; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}