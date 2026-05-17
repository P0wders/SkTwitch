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
    private final boolean returningChatter;

    private final boolean reply;
    private final @Nullable TwitchUser replyParentUser;
    private final String replyParentMessage;
    private final String replyParentMessageId;

    private final @Nullable TwitchUser threadParentUser;
    private final String threadParentMessageId;

    private final long timestamp;
    private final String customRewardId;
    private final String autoModFlags;

    private final int hypeChatAmount;
    private final String hypeChatCurrency;
    private final int hypeChatLevel;
    private final boolean hypeChatSystemMessage;

    private final String sourceRoomId;
    private final String sourceMessageId;

    public TwitchMessageEvent(TwitchUser user, TwitchChannel channel, String message,
                              String messageId, String bridgeName, boolean firstMessage,
                              boolean returningChatter,
                              boolean reply, @Nullable TwitchUser replyParentUser,
                              String replyParentMessage, String replyParentMessageId,
                              @Nullable TwitchUser threadParentUser, String threadParentMessageId,
                              long timestamp, String customRewardId, String autoModFlags,
                              int hypeChatAmount, String hypeChatCurrency, int hypeChatLevel,
                              boolean hypeChatSystemMessage,
                              String sourceRoomId, String sourceMessageId) {
        super(false);
        this.user = user;
        this.channel = channel;
        this.message = message;
        this.messageId = messageId;
        this.bridgeName = bridgeName;
        this.firstMessage = firstMessage;
        this.returningChatter = returningChatter;
        this.reply = reply;
        this.replyParentUser = replyParentUser;
        this.replyParentMessage = replyParentMessage;
        this.replyParentMessageId = replyParentMessageId;
        this.threadParentUser = threadParentUser;
        this.threadParentMessageId = threadParentMessageId == null ? "" : threadParentMessageId;
        this.timestamp = timestamp;
        this.customRewardId = customRewardId == null ? "" : customRewardId;
        this.autoModFlags = autoModFlags == null ? "" : autoModFlags;
        this.hypeChatAmount = hypeChatAmount;
        this.hypeChatCurrency = hypeChatCurrency == null ? "" : hypeChatCurrency;
        this.hypeChatLevel = hypeChatLevel;
        this.hypeChatSystemMessage = hypeChatSystemMessage;
        this.sourceRoomId = sourceRoomId == null ? "" : sourceRoomId;
        this.sourceMessageId = sourceMessageId == null ? "" : sourceMessageId;
    }

    public TwitchUser getUser() { return user; }
    public TwitchChannel getChannel() { return channel; }
    public String getMessage() { return message; }
    public String getMessageId() { return messageId; }
    public String getBridgeName() { return bridgeName; }
    public boolean isFirstMessage() { return firstMessage; }
    public boolean isReturningChatter() { return returningChatter; }
    public boolean isReply() { return reply; }
    public @Nullable TwitchUser getReplyParentUser() { return replyParentUser; }
    public String getReplyParentMessage() { return replyParentMessage; }
    public String getReplyParentMessageId() { return replyParentMessageId; }

    public @Nullable TwitchUser getThreadParentUser() { return threadParentUser; }
    public String getThreadParentMessageId() { return threadParentMessageId; }
    public boolean isThreadReply() { return threadParentUser != null || !threadParentMessageId.isEmpty(); }

    /** Server-side message timestamp (epoch millis), or 0 if not present. */
    public long getTimestamp() { return timestamp; }

    /** Channel-points custom reward id when the message was sent through a paid reward, else "". */
    public String getCustomRewardId() { return customRewardId; }
    public boolean hasCustomReward() { return !customRewardId.isEmpty(); }

    /** Raw AutoMod flags tag (categories + confidence scores), or "" if none. */
    public String getAutoModFlags() { return autoModFlags; }

    /** Hype Chat amount in the smallest currency unit (e.g. cents), or 0 if not a paid pinned message. */
    public int getHypeChatAmount() { return hypeChatAmount; }
    public String getHypeChatCurrency() { return hypeChatCurrency; }
    public int getHypeChatLevel() { return hypeChatLevel; }
    public boolean isHypeChatSystemMessage() { return hypeChatSystemMessage; }
    public boolean isHypeChat() { return hypeChatAmount > 0; }

    /** Source room id for a shared-chat message, or "" if not shared chat. */
    public String getSourceRoomId() { return sourceRoomId; }
    public String getSourceMessageId() { return sourceMessageId; }
    public boolean isSharedChat() { return !sourceRoomId.isEmpty(); }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
