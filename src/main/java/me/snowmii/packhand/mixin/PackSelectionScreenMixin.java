package me.snowmii.packhand.mixin;

import me.snowmii.packhand.Packhand;
import me.snowmii.packhand.preset.PresetManager;
import me.snowmii.packhand.screen.PackSelectionScreenAccess;
import me.snowmii.packhand.ui.PresetPanel;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackSelectionScreen.class)
public abstract class PackSelectionScreenMixin extends Screen implements PackSelectionScreenAccess {
    @Shadow
    @Final
    private PackSelectionModel model;

    @Shadow
    private @Nullable TransferableSelectionList availablePackList;

    @Shadow
    private @Nullable TransferableSelectionList selectedPackList;

    @Unique
    private boolean packhand$resourcePackScreen;

    @Unique
    private @Nullable PresetManager packhand$presetManager;

    @Unique
    private @Nullable PresetPanel packhand$presetPanel;

    @Unique
    private String packhand$selectedPreset = "";

    protected PackSelectionScreenMixin(final Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void packhand$identifyResourcePackScreen(
        final PackRepository repository,
        final Consumer<PackRepository> output,
        final Path packDir,
        final Component title,
        final CallbackInfo ci
    ) {
        this.packhand$resourcePackScreen = repository == Minecraft.getInstance().getResourcePackRepository();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void packhand$addPresetPanel(final CallbackInfo ci) {
        if (!this.packhand$resourcePackScreen || this.availablePackList == null || this.selectedPackList == null) {
            return;
        }
        if (this.packhand$presetManager == null) {
            this.packhand$presetManager = new PresetManager(Packhand.configFile("presets.json"));
        }

        int x = this.availablePackList.getX();
        int width = this.selectedPackList.getRight() - x;
        int y = this.availablePackList.getY() - PresetPanel.HEIGHT;
        this.packhand$presetPanel = new PresetPanel(
            this.font,
            x,
            y,
            width,
            this.packhand$presetManager,
            this.model,
            this,
            this.availablePackList,
            this.selectedPackList,
            this.packhand$selectedPreset,
            this::addRenderableWidget,
            this::packhand$selectPreset
        );
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void packhand$positionPresetPanel(final CallbackInfo ci) {
        if (!this.packhand$resourcePackScreen || this.availablePackList == null || this.selectedPackList == null) {
            return;
        }

        int availableHeight = Math.max(0, this.availablePackList.getHeight() - PresetPanel.HEIGHT);
        int selectedHeight = Math.max(0, this.selectedPackList.getHeight() - PresetPanel.HEIGHT);
        this.availablePackList.updateSizeAndPosition(
            this.availablePackList.getWidth(),
            availableHeight,
            this.availablePackList.getX(),
            this.availablePackList.getY() + PresetPanel.HEIGHT
        );
        this.selectedPackList.updateSizeAndPosition(
            this.selectedPackList.getWidth(),
            selectedHeight,
            this.selectedPackList.getX(),
            this.selectedPackList.getY() + PresetPanel.HEIGHT
        );

        if (this.packhand$presetPanel != null) {
            int x = this.availablePackList.getX();
            int width = this.selectedPackList.getRight() - x;
            this.packhand$presetPanel.reposition(x, this.availablePackList.getY() - PresetPanel.HEIGHT, width);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void packhand$tickPresetPanel(final CallbackInfo ci) {
        if (this.packhand$presetPanel != null) {
            this.packhand$presetPanel.tick();
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.packhand$presetPanel != null) {
            this.packhand$presetPanel.closePresetListIfOutside(event.x(), event.y());
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Unique
    private void packhand$selectPreset(final String name) {
        this.packhand$selectedPreset = name;
    }

    @Override
    public boolean packhand$isResourcePackScreen() {
        return this.packhand$resourcePackScreen;
    }

    @Override
    public TransferableSelectionList packhand$getAvailablePackList() {
        return this.availablePackList;
    }

    @Override
    public TransferableSelectionList packhand$getSelectedPackList() {
        return this.selectedPackList;
    }
}
