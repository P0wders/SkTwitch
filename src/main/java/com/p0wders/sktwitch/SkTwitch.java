package com.p0wders.sktwitch;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
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
        try {
            Class<?>[] loaded = ch.njol.skript.util.Utils.getClasses(this, "com.p0wders.sktwitch", "elements");
            for (Class<?> c : loaded) {
                log("&7Loaded: &f" + c.getName());
            }
        } catch (Throwable e) {
            log("&cClass scan failed: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            addon.loadClasses("com.p0wders.sktwitch", "elements");
        } catch (Throwable e) {
            log("&cloadClasses failed: " + e.getMessage());
            e.printStackTrace();
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