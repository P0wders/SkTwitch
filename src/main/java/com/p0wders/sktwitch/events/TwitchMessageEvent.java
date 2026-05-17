package com.p0wders.sktwitch.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TwitchMessageEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String channel, bridgeName, username, displayName, message, color;
    private final String userId, messageId, roomId, badges;
    private final boolean isSubscriber, isModerator, isBroadcaster, isVip, isTurbo, isFirstMessage;
    private final int subMonths;

    public TwitchMessageEvent(String channel, String bridgeName,
                              String username, String displayName, String message, String color,
                              String userId, String messageId, String roomId, String badges,
                              boolean isSubscriber, boolean isModerator, boolean isBroadcaster,
                              boolean isVip, boolean isTurbo, boolean isFirstMessage, int subMonths) {
        super(false);
        this.channel = channel;
        this.bridgeName = bridgeName;
        this.username = username;
        this.displayName = displayName;
        this.message = message;
        this.color = color;
        this.userId = userId;
        this.messageId = messageId;
        this.roomId = roomId;
        this.badges = badges;
        this.isSubscriber = isSubscriber;
        this.isModerator = isModerator;
        this.isBroadcaster = isBroadcaster;
        this.isVip = isVip;
        this.isTurbo = isTurbo;
        this.isFirstMessage = isFirstMessage;
        this.subMonths = subMonths;
    }

    public String getChannel() { return channel; }
    public String getBridgeName() { return bridgeName; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getMessage() { return message; }
    public String getColor() { return color; }
    public String getUserId() { return userId; }
    public String getMessageId() { return messageId; }
    public String getRoomId() { return roomId; }
    public String getBadges() { return badges; }
    public boolean isSubscriber() { return isSubscriber; }
    public boolean isModerator() { return isModerator; }
    public boolean isBroadcaster() { return isBroadcaster; }
    public boolean isVip() { return isVip; }
    public boolean isTurbo() { return isTurbo; }
    public boolean isFirstMessage() { return isFirstMessage; }
    public int getSubMonths() { return subMonths; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}