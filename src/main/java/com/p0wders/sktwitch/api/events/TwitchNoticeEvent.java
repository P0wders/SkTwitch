package com.p0wders.sktwitch.api.events;

import com.p0wders.sktwitch.TwitchChannel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when the Twitch IRC server sends a NOTICE message. Notices cover
 * server-side outcomes such as authentication failures, channel suspensions,
 * being banned from a room, slow-mode/follower-mode rejections, host start/stop,
 * and miscellaneous status text. The {@code msg-id} tag (when the tags
 * capability is requested) gives a stable machine-readable identifier such as
 * {@code msg_banned}, {@code msg_channel_suspended}, {@code msg_room_not_found}.
 */
public class TwitchNoticeEvent extends TwitchBaseEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String noticeId;   // msg-id tag value, "" if absent (no tags cap)
    private final String message;    // human-readable body of the NOTICE
    private final boolean hasChannel;

    public TwitchNoticeEvent(@Nullable TwitchChannel channel, @NotNull String bridgeName,
                             @NotNull String noticeId, @NotNull String message) {
        super(channel == null ? new TwitchChannel("#*", "") : channel, bridgeName);
        this.noticeId = noticeId;
        this.message = message;
        this.hasChannel = channel != null;
    }

    /** The Twitch {@code msg-id} (e.g. {@code msg_banned}), or "" if not present. */
    public @NotNull String getNoticeId() { return noticeId; }

    /** Human-readable notice text. */
    public @NotNull String getMessage() { return message; }

    /** True if Twitch attached this notice to a specific channel. */
    public boolean hasChannel() { return hasChannel; }

    /**
     * Heuristic: Twitch's error notice ids generally begin with {@code msg_},
     * {@code no_}, {@code unrecognized_}, {@code bad_}, {@code tos_}, or are
     * the login failure ids {@code login_required}/{@code login_unsuccessful}.
     * Useful as a quick filter in Skript.
     */
    public boolean isError() {
        if (noticeId.isEmpty()) return false;
        return noticeId.startsWith("msg_")
                || noticeId.startsWith("no_")
                || noticeId.startsWith("unrecognized_")
                || noticeId.startsWith("bad_")
                || noticeId.startsWith("tos_")
                || noticeId.equals("login_required")
                || noticeId.equals("login_unsuccessful");
    }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}