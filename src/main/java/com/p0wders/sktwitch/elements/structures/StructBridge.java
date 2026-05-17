package com.p0wders.sktwitch.elements.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import com.p0wders.sktwitch.SkTwitch;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import org.skriptlang.skript.lang.entry.util.LiteralEntryData;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"removal", "unused"})
public class StructBridge extends Structure {

    static {
        Skript.registerStructure(
                StructBridge.class,
                EntryValidator.builder()
                        .addEntryData(new KeyValueEntryData<List<String>>("channels", null, false) {
                            @Override
                            protected List<String> getValue(@NotNull String value) {
                                return Arrays.stream(value.split(","))
                                        .map(String::trim)
                                        .map(s -> s.replace("\"", ""))
                                        .map(s -> s.toLowerCase().replace("#", ""))
                                        .filter(s -> !s.isEmpty())
                                        .toList();
                            }
                        })
                        .addEntryData(new LiteralEntryData<>("oauth token", null, true, String.class))
                        .addEntryData(new LiteralEntryData<>("nickname", null, true, String.class))
                        .addEntryData(new LiteralEntryData<>("auto reconnect", true, true, Boolean.class))
                        .addEntryData(new LiteralEntryData<>("request tags", true, true, Boolean.class))
                        .addEntryData(new LiteralEntryData<>("request commands", true, true, Boolean.class))
                        .unexpectedEntryMessage(key ->
                                "Unexpected entry '" + key + "' in bridge. Valid: channels, oauth token, nickname, " +
                                        "auto reconnect, request tags, request commands.")
                        .build(),
                "define [a] [new] bridge named %string%"
        );
    }

    private String name;
    private EntryContainer container;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern,
                        SkriptParser.@NotNull ParseResult parseResult,
                        @NotNull EntryContainer entryContainer) {
        this.name = ((Literal<String>) args[0]).getSingle();
        this.container = entryContainer;
        return true;
    }

    @Override
    public boolean load() {
        @SuppressWarnings("unchecked")
        List<String> channels = container.getOptional("channels", List.class, true);
        if (channels == null || channels.isEmpty()) {
            Skript.error("StructBridge '" + name + "' must declare at least one channel via 'channels: ...'");
            return false;
        }

        com.p0wders.sktwitch.Bridge bridge = new com.p0wders.sktwitch.Bridge(
                name,
                channels,
                container.getOptional("oauth token", String.class, true),
                container.getOptional("nickname", String.class, true),
                Boolean.TRUE.equals(container.getOptional("auto reconnect", Boolean.class, true)),
                Boolean.TRUE.equals(container.getOptional("request tags", Boolean.class, true)),
                Boolean.TRUE.equals(container.getOptional("request commands", Boolean.class, true))
        );

        if (bridge.oauthToken() != null && bridge.nickname() == null) {
            Skript.error("StructBridge '" + name + "' has oauth token but no nickname; nickname is required when authenticating");
            return false;
        }

        // Defer connection until Skript finishes loading
        Bukkit.getScheduler().runTask(SkTwitch.getInstance(), () ->
                SkTwitch.getInstance().getBridgeManager().register(bridge));
        return true;
    }

    @Override
    public void unload() {
        SkTwitch.getInstance().getBridgeManager().unregister(name);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "bridge named " + name;
    }
}