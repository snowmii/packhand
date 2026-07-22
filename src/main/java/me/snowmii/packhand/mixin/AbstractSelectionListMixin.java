package me.snowmii.packhand.mixin;

import me.snowmii.packhand.ui.drag.DragState;
import me.snowmii.packhand.mixin.accessor.TransferableSelectionListAccessor;
import me.snowmii.packhand.screen.PackSelectionScreenAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {
    @Inject(method = "contentHeight", at = @At("RETURN"), cancellable = true)
    private void packhand$adjustDragContentHeight(final CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (self instanceof TransferableSelectionList list) {
            cir.setReturnValue(cir.getReturnValue() + DragState.INSTANCE.contentHeightAdjustment(list));
        }
    }

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void packhand$updateDragGap(
        final GuiGraphicsExtractor graphics,
        final int mouseX,
        final int mouseY,
        final float partialTick,
        final CallbackInfo ci
    ) {
        Object self = this;
        if (self instanceof TransferableSelectionList list) {
            DragState.INSTANCE.updateVisualOffsets(list);
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

        DragState.INSTANCE.renderIndicator(list, graphics);
        if (list == selected(list)) {
            DragState.INSTANCE.renderGhost(list, graphics);
        }
    }

    @Unique
    private static TransferableSelectionList selected(final TransferableSelectionList list) {
        PackSelectionScreen screen = ((TransferableSelectionListAccessor)list).packhand$getScreen();
        return ((PackSelectionScreenAccess)screen).packhand$getSelectedPackList();
    }
}
