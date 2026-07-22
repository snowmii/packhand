package me.snowmii.packhand.mixin;

import me.snowmii.packhand.ui.drag.DragState;
import me.snowmii.packhand.mixin.accessor.PackEntryAccessor;
import me.snowmii.packhand.mixin.accessor.TransferableSelectionListAccessor;
import me.snowmii.packhand.screen.PackSelectionScreenAccess;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerWidget.class)
public abstract class TransferableSelectionListMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void packhand$armDrag(
        final MouseButtonEvent event,
        final boolean doubleClick,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        TransferableSelectionList self = self();
        if (self == null) {
            return;
        }
        if (event.button() != 0 || !access(self).packhand$isResourcePackScreen()) {
            DragState.INSTANCE.clear();
            return;
        }

        for (TransferableSelectionList.Entry child : self.children()) {
            if (child instanceof TransferableSelectionList.PackEntry packEntry
                && packEntry.isMouseOver(event.x(), event.y())
                && event.x() >= packEntry.getContentX() + TransferableSelectionList.PackEntry.ICON_SIZE) {
                PackSelectionModel.Entry modelEntry = ((PackEntryAccessor)packEntry).packhand$getPack();
                DragState.INSTANCE.arm(self, packEntry, modelEntry, event.x(), event.y());
                return;
            }
        }
        DragState.INSTANCE.clear();
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void packhand$drag(
        final MouseButtonEvent event,
        final double dx,
        final double dy,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        TransferableSelectionList self = self();
        if (self == null || event.button() != 0 || !DragState.INSTANCE.belongsTo(self)) {
            return;
        }

        if (DragState.INSTANCE.update(available(self), selected(self), event.x(), event.y())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void packhand$drop(final MouseButtonEvent event, final CallbackInfoReturnable<Boolean> cir) {
        TransferableSelectionList self = self();
        if (self == null || event.button() != 0 || !DragState.INSTANCE.belongsTo(self)) {
            return;
        }

        if (DragState.INSTANCE.isDragging()) {
            DragState.INSTANCE.drop(available(self), selected(self));
            cir.setReturnValue(true);
        } else {
            DragState.INSTANCE.clear();
        }
    }

    private TransferableSelectionList self() {
        Object self = this;
        return self instanceof TransferableSelectionList list ? list : null;
    }

    private static PackSelectionScreenAccess access(final TransferableSelectionList list) {
        PackSelectionScreen screen = ((TransferableSelectionListAccessor)list).packhand$getScreen();
        return (PackSelectionScreenAccess)screen;
    }

    private static TransferableSelectionList available(final TransferableSelectionList list) {
        return access(list).packhand$getAvailablePackList();
    }

    private static TransferableSelectionList selected(final TransferableSelectionList list) {
        return access(list).packhand$getSelectedPackList();
    }
}
