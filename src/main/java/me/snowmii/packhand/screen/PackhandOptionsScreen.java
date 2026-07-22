package me.snowmii.packhand.screen;

import me.snowmii.packhand.config.PackhandConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class PackhandOptionsScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private final Screen parent;
    private Button arrowsButton;
    private Button animationButton;

    public PackhandOptionsScreen(final Screen parent) {
        super(Component.translatable("packhand.options.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (this.width - BUTTON_WIDTH) / 2;
        int y = this.height / 2 - 45;
        StringWidget title = new StringWidget(this.getTitle(), this.font);
        title.setPosition((this.width - title.getWidth()) / 2, y - 28);
        this.addRenderableWidget(title);

        this.arrowsButton = this.addRenderableWidget(Button.builder(arrowsLabel(), ignored -> {
            PackhandConfig config = PackhandConfig.INSTANCE;
            config.setHideArrows(!config.hideArrows());
            this.arrowsButton.setMessage(arrowsLabel());
        }).bounds(x, y, BUTTON_WIDTH, 20).build());

        this.animationButton = this.addRenderableWidget(Button.builder(animationLabel(), ignored -> {
            PackhandConfig config = PackhandConfig.INSTANCE;
            config.setAnimatedDragging(!config.animatedDragging());
            this.animationButton.setMessage(animationLabel());
        }).bounds(x, y + 24, BUTTON_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> onClose())
            .bounds(x, y + 58, BUTTON_WIDTH, 20).build());
    }

    @Override
    public void onClose() {
        ScreenNavigation.open(this.parent);
    }

    private static Component arrowsLabel() {
        return optionLabel("packhand.options.hide_arrows", PackhandConfig.INSTANCE.hideArrows());
    }

    private static Component animationLabel() {
        return optionLabel("packhand.options.animated_dragging", PackhandConfig.INSTANCE.animatedDragging());
    }

    private static Component optionLabel(final String key, final boolean value) {
        return Component.translatable(key).append(": ").append(Component.translatable(value ? "options.on" : "options.off"));
    }
}
