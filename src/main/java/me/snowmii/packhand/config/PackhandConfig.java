package me.snowmii.packhand.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import me.snowmii.packhand.Packhand;

/** Small, eagerly loaded client configuration shared by the UI mixins. */
public final class PackhandConfig {
    public static final PackhandConfig INSTANCE = new PackhandConfig(Packhand.configFile("config.json"));

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private boolean hideArrows;
    private boolean animatedDragging = true;

    private PackhandConfig(final Path file) {
        this.file = file;
        load();
    }

    public boolean hideArrows() {
        return this.hideArrows;
    }

    public boolean animatedDragging() {
        return this.animatedDragging;
    }

    public void setHideArrows(final boolean hideArrows) {
        this.hideArrows = hideArrows;
        save();
    }

    public void setAnimatedDragging(final boolean animatedDragging) {
        this.animatedDragging = animatedDragging;
        save();
    }

    private void load() {
        if (!Files.isRegularFile(this.file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                this.hideArrows = data.hideArrows;
                this.animatedDragging = data.animatedDragging;
            }
        } catch (IOException | RuntimeException ignored) {
            // Keep safe defaults when an older or manually edited file is invalid.
        }
    }

    private void save() {
        Path temporary = this.file.resolveSibling(this.file.getFileName() + ".tmp");
        try {
            Files.createDirectories(this.file.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(new Data(this.hideArrows, this.animatedDragging), writer);
            }
            Files.move(temporary, this.file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Options remain effective for this session even if persistence fails.
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
            }
        }
    }

    private static final class Data {
        private boolean hideArrows;
        private boolean animatedDragging = true;

        private Data(final boolean hideArrows, final boolean animatedDragging) {
            this.hideArrows = hideArrows;
            this.animatedDragging = animatedDragging;
        }
    }
}
