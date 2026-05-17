package com.p0wders.sktwitch;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import com.p0wders.sktwitch.api.events.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.lang.converter.Converter;

import java.io.IOException;

@SuppressWarnings({"deprecation", "removal"})
public final class SkTwitch extends JavaPlugin {

    private static SkTwitch instance;
    private BridgeManager bridgeManager;

    @Override
    public void onEnable() {
        instance = this;
        this.bridgeManager = new BridgeManager(this);

        SkriptAddon addon = Skript.registerAddon(this);

        // ClassInfos
        Classes.registerClass(new ClassInfo<>(TwitchUser.class, "twitchuser")
                .user("twitch ?users?")
                .name("Twitch User")
                .description("A user in Twitch chat")
                .parser(new Parser<TwitchUser>() {
                    @Override public boolean canParse(ParseContext context) { return false; }
                    @Override public String toString(TwitchUser u, int flags) { return u.getDisplayName(); }
                    @Override public String toVariableNameString(TwitchUser u) { return "twitchuser:" + u.getUsername(); }
                }));

        Classes.registerClass(new ClassInfo<>(TwitchChannel.class, "twitchchannel")
                .user("twitch ?channels?")
                .name("Twitch Channel")
                .description("A Twitch channel")
                .parser(new Parser<TwitchChannel>() {
                    @Override public boolean canParse(ParseContext context) { return false; }
                    @Override public String toString(TwitchChannel c, int flags) { return c.getName(); }
                    @Override public String toVariableNameString(TwitchChannel c) { return "twitchchannel:" + c.getName(); }
                }));

        // Original message event values
        registerEventValues(TwitchMessageEvent.class,
                TwitchMessageEvent::getUser,
                TwitchMessageEvent::getChannel,
                TwitchMessageEvent::getMessage);

        // New events: channel + bridge user values
        registerUserAndChannel(TwitchSubscribeEvent.class, TwitchSubscribeEvent::getUser);
        registerUserAndChannel(TwitchGiftSubEvent.class, TwitchGiftSubEvent::getRecipient);
        registerUserAndChannel(TwitchMassGiftSubEvent.class, ev -> ev.getGifter()); // may be null
        registerUserAndChannel(TwitchRaidEvent.class, TwitchRaidEvent::getRaider);
        registerUserAndChannel(TwitchAnnouncementEvent.class, TwitchAnnouncementEvent::getUser);
        registerUserAndChannel(TwitchCheerEvent.class, TwitchCheerEvent::getUser);
        registerChannelOnly(TwitchBanEvent.class);
        registerChannelOnly(TwitchClearChatEvent.class);
        registerChannelOnly(TwitchMessageDeleteEvent.class);
        registerChannelOnly(TwitchRoomStateEvent.class);

        try {
            addon.loadClasses("com.p0wders.sktwitch", "elements");
        } catch (IOException e) {
            getLogger().severe("Failed to load Skript syntax: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        log("&aSkTwitch enabled");
    }

    private <E extends TwitchBaseEvent> void registerUserAndChannel(Class<E> cls, Converter<E, TwitchUser> userGetter) {
        EventValues.registerEventValue(cls, TwitchUser.class, userGetter, 0);
        EventValues.registerEventValue(cls, TwitchChannel.class, TwitchBaseEvent::getChannel, 0);
    }

    private <E extends TwitchBaseEvent> void registerChannelOnly(Class<E> cls) {
        EventValues.registerEventValue(cls, TwitchChannel.class, TwitchBaseEvent::getChannel, 0);
    }

    private void registerEventValues(Class<TwitchMessageEvent> cls,
                                     Converter<TwitchMessageEvent, TwitchUser> userGetter,
                                     Converter<TwitchMessageEvent, TwitchChannel> channelGetter,
                                     Converter<TwitchMessageEvent, String> stringGetter) {
        EventValues.registerEventValue(cls, TwitchUser.class, userGetter, 0);
        EventValues.registerEventValue(cls, TwitchChannel.class, channelGetter, 0);
        EventValues.registerEventValue(cls, String.class, stringGetter, 0);
    }

    @Override
    public void onDisable() {
        if (bridgeManager != null) bridgeManager.shutdownAll();
    }

    public void log(String message) {
        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&', "&7[&5SkTwitch&7] &r" + message)
        );
    }

    public static SkTwitch getInstance() { return instance; }
    public BridgeManager getBridgeManager() { return bridgeManager; }
}