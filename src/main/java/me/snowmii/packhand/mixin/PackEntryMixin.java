package me.snowmii.packhand.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.snowmii.packhand.config.PackhandConfig;
import me.snowmii.packhand.ui.drag.DragState;
import me.snowmii.packhand.mixin.accessor.PackEntryAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class PackEntryMixin {
    @Inject(method = "extractContent", at = @At("HEAD"), cancellable = true)
    private void packhand$hideDraggedEntry(final CallbackInfo ci) {
        TransferableSelectionList.PackEntry self = (TransferableSelectionList.PackEntry)(Object)this;
        PackSelectionModel.Entry pack = ((PackEntryAccessor)self).packhand$getPack();
        if (DragState.INSTANCE.isDraggedEntry(pack)) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "extractContent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
        )
    )
    private void packhand$hideMoveArrows(
        final GuiGraphicsExtractor graphics,
        final RenderPipeline pipeline,
        final Identifier sprite,
        final int x,
        final int y,
        final int width,
        final int height
    ) {
        if (!PackhandConfig.INSTANCE.hideArrows() || !sprite.toString().contains("/move_")) {
            graphics.blitSprite(pipeline, sprite, x, y, width, height);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void packhand$disableHiddenArrowClicks(
        final MouseButtonEvent event,
        final boolean doubleClick,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (!PackhandConfig.INSTANCE.hideArrows()) {
            return;
        }
        TransferableSelectionList.PackEntry self = (TransferableSelectionList.PackEntry)(Object)this;
        PackSelectionModel.Entry pack = ((PackEntryAccessor)self).packhand$getPack();
        int relativeX = (int)event.x() - self.getContentX();
        int relativeY = (int)event.y() - self.getContentY();
        if (!pack.canSelect() && relativeX >= 16 && relativeX < 32 && relativeY >= 0 && relativeY < 32) {
            cir.setReturnValue(false);
        }
    }
}
