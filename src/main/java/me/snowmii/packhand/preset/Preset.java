package me.snowmii.packhand.preset;

import java.util.List;

public record Preset(String name, List<String> packs) {
    private static final String SERVER_PACK_PREFIX = "server/";

    public Preset {
        name = name == null ? "" : name.trim();
        packs = packs == null
            ? List.of()
            : packs.stream().filter(pack -> pack != null && !pack.startsWith(SERVER_PACK_PREFIX)).toList();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Preset name cannot be empty");
        }
    }
}
