package me.snowmii.packhand.screen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Bridges the screen-opening API renamed between supported Minecraft versions. */
public final class ScreenNavigation {
    private ScreenNavigation() {
    }

    public static void open(final Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        try {
            Method setter = minecraft.getClass().getMethod("setScreen", Screen.class);
            setter.invoke(minecraft, screen);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Field guiField = minecraft.getClass().getField("gui");
            Object gui = guiField.get(minecraft);
            Method setter = gui.getClass().getMethod("setScreen", Screen.class);
            setter.invoke(gui, screen);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not open screen", exception);
        }
    }
}
