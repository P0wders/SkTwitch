package com.p0wders.sktwitch;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public record Bridge(
        String name,
        List<String> channels,
        @Nullable String oauthToken,
        @Nullable String nickname,
        boolean autoReconnect,
        boolean requestTags,
        boolean requestCommands
) {}