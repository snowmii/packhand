package me.snowmii.packhand.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** A full-row preset button whose overlaid action area owns hover and clicks. */
final class PresetButton extends Button {
    private static final int ACTION_AREA_WIDTH = 40;
    private final Font font;

    PresetButton(final Font font, final Component message, final int width, final OnPress onPress) {
        super(0, 0, width, 20, message, onPress, DEFAULT_NARRATION);
        this.font = font;
    }

    @Override
    protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        extractDefaultSprite(graphics);
        graphics.centeredText(this.font, getMessage(), getX() + getWidth() / 2, getY() + 6, active ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        return isActive()
            && mouseX >= getX()
            && mouseX < getRight() - ACTION_AREA_WIDTH
            && mouseY >= getY()
            && mouseY < getBottom();
    }
}
