package me.snowmii.packhand;

import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Client entrypoint. All behaviour is installed through mixins, so this only holds shared constants. */
public final class Packhand implements ClientModInitializer {
    public static final String MOD_ID = "packhand";

    /** Returns a file inside the mod's own configuration directory. */
    public static Path configFile(final String name) {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve(name);
    }

    @Override
    public void onInitializeClient() {
        // No runtime registration needed.
    }
}
