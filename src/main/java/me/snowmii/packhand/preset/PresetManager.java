package me.snowmii.packhand.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class PresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private final List<Preset> presets = new ArrayList<>();

    public PresetManager(final Path file) {
        this.file = file;
        load();
    }

    public List<Preset> presets() {
        return List.copyOf(this.presets);
    }

    public Optional<Preset> find(final String name) {
        return this.presets.stream().filter(preset -> sameName(preset.name(), name)).findFirst();
    }

    public Preset create(final String name, final List<String> packs) {
        Preset preset = new Preset(name, packs);
        rejectDuplicate(preset.name(), null);
        this.presets.add(preset);
        try {
            save();
        } catch (RuntimeException exception) {
            this.presets.removeLast();
            throw exception;
        }
        return preset;
    }

    public Preset overwrite(final String name, final List<String> packs) {
        int index = indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown preset: " + name);
        }
        Preset preset = new Preset(this.presets.get(index).name(), packs);
        Preset previous = this.presets.set(index, preset);
        try {
            save();
        } catch (RuntimeException exception) {
            this.presets.set(index, previous);
            throw exception;
        }
        return preset;
    }

    public Preset rename(final String oldName, final String newName) {
        int index = indexOf(oldName);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown preset: " + oldName);
        }
        Preset renamed = new Preset(newName, this.presets.get(index).packs());
        rejectDuplicate(renamed.name(), oldName);
        Preset previous = this.presets.set(index, renamed);
        try {
            save();
        } catch (RuntimeException exception) {
            this.presets.set(index, previous);
            throw exception;
        }
        return renamed;
    }

    public void delete(final String name) {
        int index = indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown preset: " + name);
        }
        Preset removed = this.presets.remove(index);
        try {
            save();
        } catch (RuntimeException exception) {
            this.presets.add(index, removed);
            throw exception;
        }
    }

    private void load() {
        if (!Files.isRegularFile(this.file)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
            PresetFile data = GSON.fromJson(reader, PresetFile.class);
            if (data != null && data.presets != null) {
                for (Preset preset : data.presets) {
                    if (preset != null && !preset.name().isBlank() && indexOf(preset.name()) < 0) {
                        this.presets.add(new Preset(preset.name(), preset.packs()));
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            this.presets.clear();
            preserveCorruptFile();
        }
    }

    private void save() {
        Path parent = this.file.getParent();
        Path temporary = this.file.resolveSibling(this.file.getFileName() + ".tmp");
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(new PresetFile(this.presets), writer);
            }
            try {
                Files.move(temporary, this.file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, this.file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new PresetStorageException("Could not save presets", exception);
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
            }
        }
    }

    private void preserveCorruptFile() {
        String backupName = this.file.getFileName() + ".corrupt-" + Instant.now().toEpochMilli();
        try {
            Files.move(this.file, this.file.resolveSibling(backupName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    private int indexOf(final String name) {
        for (int index = 0; index < this.presets.size(); index++) {
            if (sameName(this.presets.get(index).name(), name)) {
                return index;
            }
        }
        return -1;
    }

    private void rejectDuplicate(final String name, final String allowedName) {
        int index = indexOf(name);
        if (index >= 0 && (allowedName == null || !sameName(this.presets.get(index).name(), allowedName))) {
            throw new IllegalArgumentException("A preset named '" + name + "' already exists");
        }
    }

    private static boolean sameName(final String left, final String right) {
        return left != null && right != null && left.toLowerCase(Locale.ROOT).equals(right.toLowerCase(Locale.ROOT));
    }

    private static final class PresetFile {
        private List<Preset> presets;

        private PresetFile(final List<Preset> presets) {
            this.presets = List.copyOf(presets);
        }
    }

    public static final class PresetStorageException extends RuntimeException {
        public PresetStorageException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
