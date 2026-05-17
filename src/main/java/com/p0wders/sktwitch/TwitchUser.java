package com.p0wders.sktwitch;

import org.jetbrains.annotations.Nullable;

public class TwitchUser {
    private final String username;
    private final String displayName;
    private final String userId;
    private final @Nullable String color;
    private final String badges;
    private final String badgeInfo;
    private final String userType;
    private final boolean subscriber, moderator, broadcaster, vip, turbo, founder;
    private final int subMonths;
    private final int exactSubMonths;

    public TwitchUser(String username, String displayName, String userId, @Nullable String color, String badges,
                      boolean subscriber, boolean moderator, boolean broadcaster, boolean vip, boolean turbo,
                      int subMonths) {
        this(username, displayName, userId, color, badges, "", "",
                subscriber, moderator, broadcaster, vip, turbo, false, subMonths, 0);
    }

    public TwitchUser(String username, String displayName, String userId, @Nullable String color, String badges,
                      String badgeInfo, String userType,
                      boolean subscriber, boolean moderator, boolean broadcaster, boolean vip, boolean turbo,
                      boolean founder, int subMonths, int exactSubMonths) {
        this.username = username;
        this.displayName = displayName;
        this.userId = userId;
        this.color = color;
        this.badges = badges;
        this.badgeInfo = badgeInfo == null ? "" : badgeInfo;
        this.userType = userType == null ? "" : userType;
        this.subscriber = subscriber;
        this.moderator = moderator;
        this.broadcaster = broadcaster;
        this.vip = vip;
        this.turbo = turbo;
        this.founder = founder;
        this.subMonths = subMonths;
        this.exactSubMonths = exactSubMonths;
    }

    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getUserId() { return userId; }

    /** Returns null if user has no color set (lets Skript's ? operator work). */
    public @Nullable String getColor() {
        return (color == null || color.isEmpty()) ? null : color;
    }

    public String getBadges() { return badges; }
    public String getBadgeInfo() { return badgeInfo; }
    public String getUserType() { return userType; }
    public boolean isSubscriber() { return subscriber; }
    public boolean isModerator() { return moderator; }
    public boolean isBroadcaster() { return broadcaster; }
    public boolean isVip() { return vip; }
    public boolean isTurbo() { return turbo; }
    public boolean isFounder() { return founder; }

    /** Sub months as encoded in the badge tier (1/3/6/12 buckets). Use {@link #getExactSubMonths()} for the real value. */
    public int getSubMonths() { return subMonths; }

    /** Exact sub month count parsed from the badge-info tag, or 0 if unavailable. */
    public int getExactSubMonths() { return exactSubMonths; }

    /** Returns the version of a badge by name (e.g. "subscriber", "bits", "moderator"), or 0 if absent. */
    public int getBadgeVersion(String name) {
        if (badges == null || badges.isEmpty() || name == null || name.isEmpty()) return 0;
        for (String entry : badges.split(",")) {
            int slash = entry.indexOf('/');
            if (slash <= 0) continue;
            if (entry.substring(0, slash).equals(name)) {
                try { return Integer.parseInt(entry.substring(slash + 1)); }
                catch (NumberFormatException e) { return 0; }
            }
        }
        return 0;
    }

    @Override
    public String toString() { return displayName; }
}
