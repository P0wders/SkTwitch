package com.p0wders.sktwitch;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.p0wders.sktwitch.api.events.TwitchMessageEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

        // TwitchUser ClassInfo
        Classes.registerClass(new ClassInfo<>(TwitchUser.class, "twitchuser")
                .user("twitch ?users?")
                .name("Twitch User")
                .description("A user in Twitch chat")
                .parser(new Parser<TwitchUser>() {
                    @Override public boolean canParse(ParseContext context) { return false; }
                    @Override public String toString(TwitchUser u, int flags) { return u.getDisplayName(); }
                    @Override public String toVariableNameString(TwitchUser u) { return "twitchuser:" + u.getUsername(); }
                }));

        // TwitchChannel ClassInfo
        Classes.registerClass(new ClassInfo<>(TwitchChannel.class, "twitchchannel")
                .user("twitch ?channels?")
                .name("Twitch Channel")
                .description("A Twitch channel")
                .parser(new Parser<TwitchChannel>() {
                    @Override public boolean canParse(ParseContext context) { return false; }
                    @Override public String toString(TwitchChannel c, int flags) { return c.getName(); }
                    @Override public String toVariableNameString(TwitchChannel c) { return "twitchchannel:" + c.getName(); }
                }));

        // event-twitchuser
        EventValues.registerEventValue(TwitchMessageEvent.class, TwitchUser.class,
                new Getter<TwitchUser, TwitchMessageEvent>() {
                    @Override public TwitchUser get(TwitchMessageEvent e) { return e.getUser(); }
                }, 0);

        // event-twitchchannel
        EventValues.registerEventValue(TwitchMessageEvent.class, TwitchChannel.class,
                new Getter<TwitchChannel, TwitchMessageEvent>() {
                    @Override public TwitchChannel get(TwitchMessageEvent e) { return e.getChannel(); }
                }, 0);

        // event-string = message body
        EventValues.registerEventValue(TwitchMessageEvent.class, String.class,
                new Getter<String, TwitchMessageEvent>() {
                    @Override public String get(TwitchMessageEvent e) { return e.getMessage(); }
                }, 0);

        try {
            addon.loadClasses("com.p0wders.sktwitch", "elements");
        } catch (IOException e) {
            getLogger().severe("Failed to load Skript syntax: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        log("&aSkTwitch enabled");
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