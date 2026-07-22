package me.snowmii.packhand.preset;

import java.util.List;

public record Preset(String name, List<String> packs) {
    public Preset {
        name = name == null ? "" : name.trim();
        packs = packs == null ? List.of() : List.copyOf(packs);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Preset name cannot be empty");
        }
    }
}
