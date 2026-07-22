package me.snowmii.packhand.mixin;

import me.snowmii.packhand.ui.drag.DragState;
import me.snowmii.packhand.mixin.accessor.TransferableSelectionListAccessor;
import me.snowmii.packhand.screen.PackSelectionScreenAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {
    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void packhand$animateDragGap(
        final GuiGraphicsExtractor graphics,
        final int mouseX,
        final int mouseY,
        final float partialTick,
        final CallbackInfo ci
    ) {
        Object self = this;
        if (self instanceof TransferableSelectionList list) {
            DragState.INSTANCE.applyVisualOffsets(list);
        }
    }

    @Inject(method = "extractWidgetRenderState", at = @At("TAIL"))
    private void packhand$renderDrag(
        final GuiGraphicsExtractor graphics,
        final int mouseX,
        final int mouseY,
        final float partialTick,
        final CallbackInfo ci
    ) {
        Object self = this;
        if (!(self instanceof TransferableSelectionList list)) {
            return;
        }

        DragState.INSTANCE.restoreVisualOffsets();
        DragState.INSTANCE.renderIndicator(list, graphics);
        if (list == selected(list)) {
            DragState.INSTANCE.renderGhost(list, graphics);
        }
    }

    private static TransferableSelectionList selected(final TransferableSelectionList list) {
        PackSelectionScreen screen = ((TransferableSelectionListAccessor)list).packhand$getScreen();
        return ((PackSelectionScreenAccess)screen).packhand$getSelectedPackList();
    }
}
