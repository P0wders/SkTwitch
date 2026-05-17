package com.p0wders.sktwitch;

public class TwitchChannel {
    private final String name;
    private final String roomId;

    public TwitchChannel(String name, String roomId) {
        this.name = name.startsWith("#") ? name : "#" + name;
        this.roomId = roomId;
    }

    public String getName() { return name; }
    public String getNameWithoutHash() { return name.substring(1); }
    public String getRoomId() { return roomId; }

    @Override
    public String toString() { return name; }
}