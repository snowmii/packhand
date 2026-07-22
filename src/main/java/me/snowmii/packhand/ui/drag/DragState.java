package me.snowmii.packhand.ui.drag;

import me.snowmii.packhand.config.PackhandConfig;
import me.snowmii.packhand.mixin.accessor.PackEntryAccessor;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.FormattedCharSequence;

/** Owns the single drag gesture shared by the two pack lists on a pack screen. */
public final class DragState {
    public static final DragState INSTANCE = new DragState();

    private static final double START_DISTANCE_SQUARED = 16.0;
    private static final int AUTO_SCROLL_ZONE = 18;
    private static final double AUTO_SCROLL_STEP = 8.0;

    private TransferableSelectionList origin;
    private PackSelectionModel.Entry entry;
    private double startX;
    private double startY;
    private int grabOffsetX;
    private int grabOffsetY;
    private double mouseX;
    private double mouseY;
    private boolean dragging;
    private TransferableSelectionList target;
    private int targetIndex;
    private final Map<TransferableSelectionList.PackEntry, Double> visualOffsets = new IdentityHashMap<>();
    private final Map<TransferableSelectionList.PackEntry, Integer> appliedOffsets = new IdentityHashMap<>();
    private double ghostX;
    private double ghostY;

    private DragState() {
    }

    public void arm(
        final TransferableSelectionList origin,
        final TransferableSelectionList.PackEntry widget,
        final PackSelectionModel.Entry entry,
        final double mouseX,
        final double mouseY
    ) {
        clear();
        this.origin = origin;
        this.entry = entry;
        this.startX = mouseX;
        this.startY = mouseY;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.grabOffsetX = (int)mouseX - widget.getContentX();
        this.grabOffsetY = (int)mouseY - widget.getContentY();
        this.ghostX = widget.getContentX();
        this.ghostY = widget.getContentY();
    }

    public boolean belongsTo(final TransferableSelectionList list) {
        return this.origin == list;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public boolean isDraggedEntry(final PackSelectionModel.Entry candidate) {
        return this.dragging && this.entry == candidate;
    }

    public boolean update(
        final TransferableSelectionList available,
        final TransferableSelectionList selected,
        final double mouseX,
        final double mouseY
    ) {
        if (this.origin == null || this.entry == null) {
            return false;
        }

        this.mouseX = mouseX;
        this.mouseY = mouseY;
        if (!this.dragging) {
            double dx = mouseX - this.startX;
            double dy = mouseY - this.startY;
            if (dx * dx + dy * dy < START_DISTANCE_SQUARED) {
                return false;
            }
            this.dragging = true;
            this.origin.setSelected(null);
        }

        this.target = listAt(available, selected, mouseX, mouseY);
        // The dragged row is removed from the origin's visual layout and added to
        // the target's. Keep the scroll bounds in sync with that temporary layout.
        available.refreshScrollAmount();
        selected.refreshScrollAmount();
        if (this.target != null) {
            autoScroll(this.target, mouseY);
            this.targetIndex = insertionIndex(this.target, this.entry.getId(), mouseY);
        }
        return true;
    }

    public void drop(final TransferableSelectionList available, final TransferableSelectionList selected) {
        if (!this.dragging || this.entry == null || this.target == null) {
            clear();
            return;
        }

        boolean fromSelected = this.entry.isSelected();
        boolean toSelected = this.target == selected;
        if (fromSelected && !toSelected) {
            if (this.entry.canUnselect()) {
                this.entry.unselect();
            }
        } else if (!fromSelected && toSelected) {
            if (this.entry.canSelect()) {
                String id = this.entry.getId();
                this.entry.select();
                PackSelectionModel.Entry selectedEntry = modelEntry(selected, id);
                if (selectedEntry != null) {
                    moveTo(selectedEntry, selected, this.targetIndex);
                }
            }
        } else if (fromSelected && toSelected) {
            moveTo(this.entry, selected, this.targetIndex);
        }

        clear();
    }

    public void renderIndicator(final TransferableSelectionList list, final GuiGraphicsExtractor graphics) {
        if (!this.dragging || this.target != list || this.entry == null) {
            return;
        }

        List<TransferableSelectionList.PackEntry> entries = packEntries(list, this.entry.getId());
        int originIndex = list == this.origin ? packIndex(list, this.entry.getId()) : -1;
        int y;
        if (entries.isEmpty()) {
            y = list.getY() + 18;
        } else if (this.targetIndex < entries.size()) {
            TransferableSelectionList.PackEntry targetEntry = entries.get(this.targetIndex);
            y = collapsedY(targetEntry, this.targetIndex, originIndex);
        } else {
            TransferableSelectionList.PackEntry last = entries.getLast();
            y = collapsedY(last, entries.size() - 1, originIndex) + last.getHeight();
        }
        int color = PackhandConfig.INSTANCE.animatedDragging() ? 0xFF66CCFF : 0xFFFFFFFF;
        graphics.fill(list.getX() + 4, y - 1, list.getRight() - 4, y + 1, color);
    }

    public void renderGhost(final TransferableSelectionList list, final GuiGraphicsExtractor graphics) {
        if (!this.dragging || this.entry == null) {
            return;
        }

        int width = Math.max(80, list.getRowWidth());
        double desiredX = this.mouseX - this.grabOffsetX;
        double desiredY = this.mouseY - this.grabOffsetY;
        if (PackhandConfig.INSTANCE.animatedDragging()) {
            this.ghostX += (desiredX - this.ghostX) * 0.55;
            this.ghostY += (desiredY - this.ghostY) * 0.55;
        } else {
            this.ghostX = desiredX;
            this.ghostY = desiredY;
        }
        int x = (int)Math.round(this.ghostX);
        int y = (int)Math.round(this.ghostY);
        graphics.fill(x + 3, y + 3, x + width + 4, y + 36, 0x70000000);
        graphics.fill(x - 1, y - 1, x + width + 1, y + 33, 0xE066CCFF);
        graphics.fill(x, y, x + width, y + 32, 0xEE202020);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.entry.getIconTexture(), x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        var font = Minecraft.getInstance().font;
        graphics.text(font, this.entry.getTitle(), x + 35, y + 2, 0xFFFFFFFF);
        List<FormattedCharSequence> description = font.split(this.entry.getExtendedDescription(), Math.max(1, width - 37));
        for (int line = 0; line < Math.min(2, description.size()); line++) {
            graphics.text(font, description.get(line), x + 35, y + 12 + line * 9, 0xFF808080, false);
        }
    }

    /** Advances the surrounding rows toward the gap opened at the drop position. */
    public void updateVisualOffsets(final TransferableSelectionList list) {
        if (!this.dragging) {
            return;
        }
        List<TransferableSelectionList.PackEntry> all = packEntries(list, null);
        int originIndex = packIndex(list, this.entry.getId());
        for (int index = 0; index < all.size(); index++) {
            TransferableSelectionList.PackEntry widget = all.get(index);
            if (widget.getPackId().equals(this.entry.getId())) {
                continue;
            }
            double desired = desiredOffset(list, widget, index, originIndex);
            double current = this.visualOffsets.getOrDefault(widget, 0.0);
            if (PackhandConfig.INSTANCE.animatedDragging()) {
                current += (desired - current) * 0.38;
            } else {
                current = desired;
            }
            if (Math.abs(current) < 0.35 && desired == 0.0) {
                current = 0.0;
            }
            this.visualOffsets.put(widget, current);
        }
    }

    /** Applies an offset only while the row extracts its render state. */
    public void applyVisualOffset(final TransferableSelectionList.PackEntry widget) {
        int offset = (int)Math.round(this.visualOffsets.getOrDefault(widget, 0.0));
        if (offset != 0) {
            widget.setY(widget.getY() + offset);
            this.appliedOffsets.put(widget, offset);
        }
    }

    public void restoreVisualOffset(final TransferableSelectionList.PackEntry widget) {
        Integer offset = this.appliedOffsets.remove(widget);
        if (offset != null) {
            widget.setY(widget.getY() - offset);
        }
    }

    private void restoreVisualOffsets() {
        this.appliedOffsets.forEach((widget, offset) -> widget.setY(widget.getY() - offset));
        this.appliedOffsets.clear();
    }

    /** Mirrors the temporary drag gap in the list's scrollable content height. */
    public int contentHeightAdjustment(final TransferableSelectionList list) {
        if (!this.dragging || this.entry == null || this.target == this.origin) {
            return 0;
        }

        int draggedHeight = draggedEntryHeight();
        if (list == this.origin) {
            return -draggedHeight;
        }
        return list == this.target ? draggedHeight : 0;
    }

    private double desiredOffset(
        final TransferableSelectionList list,
        final TransferableSelectionList.PackEntry widget,
        final int index,
        final int originIndex
    ) {
        int height = widget.getHeight();
        if (list == this.origin && list != this.target) {
            return index > originIndex ? -height : 0.0;
        }
        if (list != this.target) {
            return 0.0;
        }
        if (list != this.origin) {
            return index >= this.targetIndex ? height : 0.0;
        }
        if (originIndex == this.targetIndex) {
            return 0.0;
        }
        if (originIndex < this.targetIndex && index > originIndex && index <= this.targetIndex) {
            return -height;
        }
        if (originIndex > this.targetIndex && index >= this.targetIndex && index < originIndex) {
            return height;
        }
        return 0.0;
    }

    public void clear() {
        TransferableSelectionList previousOrigin = this.origin;
        TransferableSelectionList previousTarget = this.target;
        this.origin = null;
        this.entry = null;
        this.target = null;
        this.dragging = false;
        this.targetIndex = 0;
        restoreVisualOffsets();
        this.visualOffsets.clear();
        if (previousOrigin != null) {
            previousOrigin.refreshScrollAmount();
        }
        if (previousTarget != null && previousTarget != previousOrigin) {
            previousTarget.refreshScrollAmount();
        }
    }

    private int draggedEntryHeight() {
        if (this.origin == null || this.entry == null) {
            return 0;
        }
        TransferableSelectionList.PackEntry widget = packEntry(this.origin, this.entry.getId());
        return widget == null ? 0 : widget.getHeight();
    }

    private static TransferableSelectionList listAt(
        final TransferableSelectionList available,
        final TransferableSelectionList selected,
        final double mouseX,
        final double mouseY
    ) {
        if (available.isMouseOver(mouseX, mouseY)) {
            return available;
        }
        return selected.isMouseOver(mouseX, mouseY) ? selected : null;
    }

    private static void autoScroll(final TransferableSelectionList list, final double mouseY) {
        if (mouseY < list.getY() + AUTO_SCROLL_ZONE) {
            list.setScrollAmount(list.scrollAmount() - AUTO_SCROLL_STEP);
        } else if (mouseY > list.getBottom() - AUTO_SCROLL_ZONE) {
            list.setScrollAmount(list.scrollAmount() + AUTO_SCROLL_STEP);
        }
    }

    private int insertionIndex(final TransferableSelectionList list, final String draggedId, final double mouseY) {
        List<TransferableSelectionList.PackEntry> entries = packEntries(list, draggedId);
        int originIndex = list == this.origin ? packIndex(list, draggedId) : -1;
        int index = 0;
        for (TransferableSelectionList.PackEntry entry : entries) {
            if (mouseY < collapsedY(entry, index, originIndex) + entry.getHeight() / 2.0) {
                break;
            }
            index++;
        }
        return index;
    }

    private static int collapsedY(
        final TransferableSelectionList.PackEntry entry,
        final int index,
        final int originIndex
    ) {
        return entry.getY() - (originIndex >= 0 && index >= originIndex ? entry.getHeight() : 0);
    }

    private static List<TransferableSelectionList.PackEntry> packEntries(final TransferableSelectionList list, final String excludedId) {
        List<TransferableSelectionList.PackEntry> result = new ArrayList<>();
        for (TransferableSelectionList.Entry child : list.children()) {
            if (child instanceof TransferableSelectionList.PackEntry packEntry
                && (excludedId == null || !packEntry.getPackId().equals(excludedId))) {
                result.add(packEntry);
            }
        }
        return result;
    }

    private static PackSelectionModel.Entry modelEntry(final TransferableSelectionList list, final String id) {
        TransferableSelectionList.PackEntry widget = packEntry(list, id);
        return widget == null ? null : ((PackEntryAccessor)widget).packhand$getPack();
    }

    private static TransferableSelectionList.PackEntry packEntry(final TransferableSelectionList list, final String id) {
        for (TransferableSelectionList.Entry child : list.children()) {
            if (child instanceof TransferableSelectionList.PackEntry packEntry && packEntry.getPackId().equals(id)) {
                return packEntry;
            }
        }
        return null;
    }

    private static void moveTo(final PackSelectionModel.Entry entry, final TransferableSelectionList list, final int requestedIndex) {
        int current = packIndex(list, entry.getId());
        if (current < 0) {
            return;
        }

        int target = Math.max(0, Math.min(requestedIndex, packEntries(list, entry.getId()).size()));
        int guard = 4096;
        while (current > target && entry.canMoveUp() && guard-- > 0) {
            entry.moveUp();
            current = packIndex(list, entry.getId());
        }
        while (current < target && entry.canMoveDown() && guard-- > 0) {
            entry.moveDown();
            current = packIndex(list, entry.getId());
        }
    }

    private static int packIndex(final TransferableSelectionList list, final String id) {
        int index = 0;
        for (TransferableSelectionList.Entry child : list.children()) {
            if (child instanceof TransferableSelectionList.PackEntry packEntry) {
                if (packEntry.getPackId().equals(id)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }
}
