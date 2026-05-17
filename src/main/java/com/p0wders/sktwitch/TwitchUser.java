package com.p0wders.sktwitch;

import org.jetbrains.annotations.Nullable;

public class TwitchUser {
    private final String username;
    private final String displayName;
    private final String userId;
    private final @Nullable String color;
    private final String badges;
    private final boolean subscriber, moderator, broadcaster, vip, turbo;
    private final int subMonths;

    public TwitchUser(String username, String displayName, String userId, @Nullable String color, String badges,
                      boolean subscriber, boolean moderator, boolean broadcaster, boolean vip, boolean turbo,
                      int subMonths) {
        this.username = username;
        this.displayName = displayName;
        this.userId = userId;
        this.color = color;
        this.badges = badges;
        this.subscriber = subscriber;
        this.moderator = moderator;
        this.broadcaster = broadcaster;
        this.vip = vip;
        this.turbo = turbo;
        this.subMonths = subMonths;
    }

    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getUserId() { return userId; }

    /** Returns null if user has no color set (lets Skript's ? operator work). */
    public @Nullable String getColor() {
        return (color == null || color.isEmpty()) ? null : color;
    }

    public String getBadges() { return badges; }
    public boolean isSubscriber() { return subscriber; }
    public boolean isModerator() { return moderator; }
    public boolean isBroadcaster() { return broadcaster; }
    public boolean isVip() { return vip; }
    public boolean isTurbo() { return turbo; }
    public int getSubMonths() { return subMonths; }

    @Override
    public String toString() { return displayName; }
}