package me.snowmii.packhand.ui;

import me.snowmii.packhand.preset.Preset;
import me.snowmii.packhand.preset.PresetManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import me.snowmii.packhand.screen.PackhandOptionsScreen;
import me.snowmii.packhand.screen.ScreenNavigation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class PresetPanel {
    public static final int HEIGHT = 24;

    private static final int WIDGET_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int SELECTOR_WIDTH = 220;
    private static final int SAVE_WIDTH = 64;
    private static final int CONFIG_WIDTH = 20;
    private static final int ROW_ACTION_WIDTH = 20;
    private static final SystemToast.SystemToastId NOTICE_ID = new SystemToast.SystemToastId();

    private final PresetManager manager;
    private final PackSelectionModel model;
    private final Font font;
    private final Screen screen;
    private final AbstractWidget availablePackList;
    private final AbstractWidget selectedPackList;
    private final Consumer<AbstractWidget> addWidget;
    private final Consumer<String> onSelectionChanged;
    private final Button selector;
    private final Button saveButton;
    private final Button configButton;
    private final DropdownFrame dropdownFrame = new DropdownFrame();
    private final EditBox nameBox;
    private final Button createButton;
    private final Button cancelButton;
    private final List<Button> presetButtons = new ArrayList<>();
    private String selectedPreset;
    private String editingPreset = "";
    private String deleteArmedPreset = "";
    private boolean presetListExpanded;

    public PresetPanel(
        final Font font,
        final int x,
        final int y,
        final int width,
        final PresetManager manager,
        final PackSelectionModel model,
        final Screen screen,
        final AbstractWidget availablePackList,
        final AbstractWidget selectedPackList,
        final String selectedName,
        final Consumer<AbstractWidget> addWidget,
        final Consumer<String> onSelectionChanged
    ) {
        this.manager = manager;
        this.model = model;
        this.font = font;
        this.screen = screen;
        this.availablePackList = availablePackList;
        this.selectedPackList = selectedPackList;
        this.addWidget = addWidget;
        this.onSelectionChanged = onSelectionChanged;

        List<String> names = presetChoices();
        this.selectedPreset = names.stream().filter(name -> name.equalsIgnoreCase(selectedName)).findFirst().orElse(names.isEmpty() ? "" : names.getFirst());

        this.selector = Button.builder(selectorLabel(), button -> togglePresetList()).size(SELECTOR_WIDTH, WIDGET_HEIGHT).build();
        this.saveButton = Button.builder(Component.translatable("packhand.preset.save"), button -> overwrite()).size(SAVE_WIDTH, WIDGET_HEIGHT).build();
        this.configButton = new IconButton(
            this.font,
            Component.literal("⛭").setStyle(Style.EMPTY.withBold(true)),
            0xFFFFFFFF,
            true,
            button -> ScreenNavigation.open(new PackhandOptionsScreen(this.screen))
        );
        this.configButton.setTooltip(Tooltip.create(Component.translatable("packhand.options.open")));

        this.nameBox = new EditBox(font, 0, 0, 200, WIDGET_HEIGHT, Component.translatable("packhand.preset.name"));
        this.nameBox.setHint(Component.translatable("packhand.preset.name.hint"));
        this.nameBox.setMaxLength(64);
        this.createButton = Button.builder(Component.translatable("packhand.preset.create"), button -> submitEdit()).size(60, WIDGET_HEIGHT).build();
        this.nameBox.setResponder(value -> this.createButton.active = !value.isBlank());
        this.cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> finishEditing()).size(60, WIDGET_HEIGHT).build();

        addWidget.accept(this.selector);
        addWidget.accept(this.saveButton);
        addWidget.accept(this.configButton);
        addWidget.accept(this.dropdownFrame);
        addWidget.accept(this.nameBox);
        addWidget.accept(this.createButton);
        addWidget.accept(this.cancelButton);
        rebuildPresetButtons();

        finishEditing();
        updateButtons();
        reposition(x, y, width);
    }

    public void reposition(final int x, final int y, final int width) {
        int controlsWidth = this.selector.getWidth() + this.saveButton.getWidth() + this.configButton.getWidth() + GAP * 2;
        int cursor = x + Math.max(0, (width - controlsWidth) / 2);
        position(this.selector, cursor, y);
        cursor += this.selector.getWidth() + GAP;
        position(this.saveButton, cursor, y);
        cursor += this.saveButton.getWidth() + GAP;
        position(this.configButton, cursor, y);

        positionPresetButtons();

        int editorWidth = this.nameBox.getWidth() + this.createButton.getWidth() + this.cancelButton.getWidth() + GAP * 2;
        cursor = x + Math.max(0, (width - editorWidth) / 2);
        position(this.nameBox, cursor, y);
        cursor += this.nameBox.getWidth() + GAP;
        position(this.createButton, cursor, y);
        cursor += this.createButton.getWidth() + GAP;
        position(this.cancelButton, cursor, y);
    }

    private void positionPresetButtons() {
        for (int index = 0; index < this.presetButtons.size(); index++) {
            Button button = this.presetButtons.get(index);
            int row = index / 3;
            int column = index % 3;
            int rowY = this.selector.getY() + WIDGET_HEIGHT + row * WIDGET_HEIGHT;
            int rowWidth = this.selector.getWidth() - 2 * GAP;
            int buttonX = this.selector.getX() + GAP;
            if (column > 0) {
                buttonX += rowWidth - (3 - column) * ROW_ACTION_WIDTH - GAP;
            }
            position(button, buttonX, rowY);
        }

        if (!this.presetButtons.isEmpty()) {
            Button newPresetButton = this.presetButtons.getLast();
            int newPresetRow = this.manager.presets().size();
            newPresetButton.setWidth(this.selector.getWidth() - 2 * GAP);
            position(newPresetButton, this.selector.getX() + GAP, this.selector.getY() + WIDGET_HEIGHT + newPresetRow * WIDGET_HEIGHT);
        }
        this.dropdownFrame.setRectangle(
            this.selector.getWidth() - GAP,
            (this.manager.presets().size() + 1) * WIDGET_HEIGHT + 3,
            this.selector.getX() + (GAP / 2),
            this.selector.getBottom() - 1
        );
    }

    private void load(final String name) {
        this.manager.find(name).ifPresent(preset -> {
            List<String> missing = apply(preset);
            if (missing.isEmpty()) {
                notice(Component.translatable("packhand.preset.loaded"), Component.literal(preset.name()));
            } else {
                String names = String.join(", ", missing);
                notice(
                    Component.translatable("packhand.preset.missing", missing.size()),
                    Component.literal(names.length() > 180 ? names.substring(0, 177) + "..." : names)
                );
            }
        });
    }

    private void submitEdit() {
        if (this.editingPreset.isEmpty()) {
            create();
        } else {
            rename();
        }
    }

    private void create() {
        try {
            Preset preset = this.manager.create(this.nameBox.getValue(), selectedIds());
            notice(Component.translatable("packhand.preset.saved"), Component.literal(preset.name()));
            selectPreset(preset.name());
            rebuildPresetButtons();
            finishEditing();
            updateButtons();
        } catch (IllegalArgumentException exception) {
            notice(Component.translatable("packhand.preset.save_failed"), Component.literal(exception.getMessage()));
        } catch (PresetManager.PresetStorageException exception) {
            notice(Component.translatable("packhand.preset.save_failed"), Component.literal(exception.getMessage()));
        }
    }

    private void rename() {
        try {
            Preset preset = this.manager.rename(this.editingPreset, this.nameBox.getValue());
            notice(Component.translatable("packhand.preset.renamed"), Component.literal(preset.name()));
            selectPreset(preset.name());
            rebuildPresetButtons();
            finishEditing();
        } catch (IllegalArgumentException | PresetManager.PresetStorageException exception) {
            notice(Component.translatable("packhand.preset.rename_failed"), Component.literal(exception.getMessage()));
        }
    }

    private void overwrite() {
        try {
            Preset preset = this.manager.overwrite(this.selectedPreset, selectedIds());
            notice(Component.translatable("packhand.preset.overwritten"), Component.literal(preset.name()));
            updateButtons();
        } catch (IllegalArgumentException | PresetManager.PresetStorageException exception) {
            notice(Component.translatable("packhand.preset.save_failed"), Component.literal(exception.getMessage()));
        }
    }

    private void delete(final String deletedName) {
        try {
            this.manager.delete(deletedName);
            String next = deletedName.equalsIgnoreCase(this.selectedPreset)
                ? (this.manager.presets().isEmpty() ? "" : this.manager.presets().getFirst().name())
                : this.selectedPreset;
            notice(Component.translatable("packhand.preset.deleted"), Component.literal(deletedName));
            selectPreset(next);
            rebuildPresetButtons();
            updateButtons();
        } catch (IllegalArgumentException | PresetManager.PresetStorageException exception) {
            notice(Component.translatable("packhand.preset.delete_failed"), Component.literal(exception.getMessage()));
        }
    }

    private List<String> apply(final Preset preset) {
        Map<String, PackSelectionModel.Entry> known = entriesById(this.model.getSelected().toList());
        entriesById(this.model.getUnselected().toList()).forEach(known::putIfAbsent);
        List<String> missing = preset.packs().stream().filter(id -> !known.containsKey(id)).toList();

        for (PackSelectionModel.Entry entry : this.model.getSelected().toList()) {
            if (entry.canUnselect()) {
                entry.unselect();
            }
        }

        for (String id : preset.packs()) {
            PackSelectionModel.Entry entry = entriesById(this.model.getUnselected().toList()).get(id);
            if (entry != null && entry.canSelect()) {
                entry.select();
            }
        }

        for (int target = 0; target < preset.packs().size(); target++) {
            moveTo(preset.packs().get(target), target);
        }
        return missing;
    }

    private void moveTo(final String id, final int requestedIndex) {
        PackSelectionModel.Entry entry = entriesById(this.model.getSelected().toList()).get(id);
        if (entry == null) {
            return;
        }
        int current = selectedIndex(id);
        int target = Math.min(requestedIndex, Math.max(0, selectedIds().size() - 1));
        int guard = 4096;
        while (current > target && entry.canMoveUp() && guard-- > 0) {
            entry.moveUp();
            current = selectedIndex(id);
        }
        while (current < target && entry.canMoveDown() && guard-- > 0) {
            entry.moveDown();
            current = selectedIndex(id);
        }
    }

    private int selectedIndex(final String id) {
        List<String> selected = selectedIds();
        return selected.indexOf(id);
    }

    private List<String> selectedIds() {
        return this.model.getSelected().map(PackSelectionModel.Entry::getId).toList();
    }

    private static Map<String, PackSelectionModel.Entry> entriesById(final List<PackSelectionModel.Entry> entries) {
        Map<String, PackSelectionModel.Entry> result = new LinkedHashMap<>();
        for (PackSelectionModel.Entry entry : entries) {
            result.put(entry.getId(), entry);
        }
        return result;
    }

    private void startEditing(final String presetName) {
        setPresetListExpanded(false);
        this.editingPreset = presetName;
        this.selector.visible = false;
        this.saveButton.visible = false;
        this.configButton.visible = false;
        this.nameBox.visible = true;
        this.nameBox.active = true;
        this.createButton.setMessage(Component.translatable(presetName.isEmpty() ? "packhand.preset.create" : "packhand.preset.rename"));
        this.createButton.visible = true;
        this.cancelButton.visible = true;
        this.cancelButton.active = true;
        this.nameBox.setValue(presetName);
        this.screen.setFocused(this.nameBox);
        this.nameBox.setFocused(true);
    }

    private void finishEditing() {
        this.editingPreset = "";
        this.selector.visible = true;
        this.saveButton.visible = true;
        this.configButton.visible = true;
        this.nameBox.visible = false;
        this.nameBox.active = false;
        this.createButton.visible = false;
        this.createButton.active = false;
        this.cancelButton.visible = false;
        this.cancelButton.active = false;
    }

    private void updateButtons() {
        boolean hasPreset = !this.selectedPreset.isEmpty();
        this.selector.active = true;
        this.saveButton.active = hasPreset && this.manager.find(this.selectedPreset)
            .map(preset -> !preset.packs().equals(selectedIds()))
            .orElse(false);
    }

    public void tick() {
        if (!this.editingPreset.isEmpty() || this.nameBox.visible) {
            return;
        }
        updateButtons();
    }

    public void closePresetListIfOutside(final double mouseX, final double mouseY) {
        if (!this.presetListExpanded) {
            return;
        }
        boolean insideSelector = this.selector.isMouseOver(mouseX, mouseY);
        boolean insideList = mouseX >= this.dropdownFrame.getX() && mouseX < this.dropdownFrame.getRight()
            && mouseY >= this.dropdownFrame.getY() && mouseY < this.dropdownFrame.getBottom();
        if (!insideSelector && !insideList) {
            setPresetListExpanded(false);
        }
    }

    private void togglePresetList() {
        setPresetListExpanded(!this.presetListExpanded);
    }

    private void setPresetListExpanded(final boolean expanded) {
        this.presetListExpanded = expanded;
        if (!expanded) {
            resetDeleteConfirmation();
        }
        this.selector.setMessage(selectorLabel());
        this.availablePackList.active = !this.presetListExpanded;
        this.selectedPackList.active = !this.presetListExpanded;
        this.dropdownFrame.visible = this.presetListExpanded;
        for (Button button : this.presetButtons) {
            button.visible = this.presetListExpanded;
            button.active = this.presetListExpanded;
        }
    }

    private void rebuildPresetButtons() {
        for (Button button : this.presetButtons) {
            button.visible = false;
            button.active = false;
        }
        this.presetButtons.clear();

        for (String choice : presetChoices()) {
            Button selectButton = new PresetButton(this.font, Component.literal(choice), this.selector.getWidth() - 2 * GAP, ignored -> {
                selectPreset(choice);
                setPresetListExpanded(false);
                load(choice);
            });
            Button renameButton = new IconButton(this.font, Component.literal("✎"), 0xFFFFFFFF, ignored -> startEditing(choice));
            renameButton.setTooltip(Tooltip.create(Component.translatable("packhand.preset.rename")));
            Button removeButton = new IconButton(this.font, Component.literal("×"), 0xFFFF5555, button -> {
                if (!choice.equalsIgnoreCase(this.deleteArmedPreset)) {
                    resetDeleteConfirmation();
                    this.deleteArmedPreset = choice;
                    button.setMessage(Component.literal("!"));
                } else {
                    delete(choice);
                }
            });
            removeButton.setTooltip(Tooltip.create(Component.translatable("packhand.preset.remove")));
            addPresetButton(selectButton);
            addPresetButton(renameButton);
            addPresetButton(removeButton);
        }

        Button newPresetButton = Button.builder(Component.translatable("packhand.preset.new"), ignored -> startEditing(""))
            .size(this.selector.getWidth(), WIDGET_HEIGHT).build();
        addPresetButton(newPresetButton);
        positionPresetButtons();
        this.deleteArmedPreset = "";
        setPresetListExpanded(false);
    }

    private void resetDeleteConfirmation() {
        this.deleteArmedPreset = "";
        for (int index = 2; index < this.presetButtons.size() - 1; index += 3) {
            this.presetButtons.get(index).setMessage(Component.literal("×"));
        }
    }

    private void addPresetButton(final Button button) {
        button.visible = false;
        button.active = false;
        this.presetButtons.add(button);
        this.addWidget.accept(button);
    }

    private void selectPreset(final String name) {
        this.selectedPreset = name;
        this.selector.setMessage(selectorLabel());
        resetDeleteConfirmation();
        this.onSelectionChanged.accept(name);
        updateButtons();
    }

    private static Component presetLabel(final String name) {
        return name.isEmpty() ? Component.translatable("packhand.preset.none") : Component.literal(name);
    }

    private Component selectorLabel() {
        return Component.translatable("packhand.preset.selector", presetLabel(this.selectedPreset))
            .append(this.presetListExpanded ? " \u25b3" : " \u25bd");
    }

    private List<String> presetChoices() {
        return this.manager.presets().stream().map(Preset::name).toList();
    }

    private static void position(final AbstractWidget widget, final int x, final int y) {
        widget.setX(x);
        widget.setY(y);
    }

    private static void notice(final Component title, final Component detail) {
        ToastManager manager = toastManager(Minecraft.getInstance());
        if (manager != null) {
            SystemToast.addOrUpdate(manager, NOTICE_ID, title, detail);
        }
    }

    private static ToastManager toastManager(final Minecraft minecraft) {
        try {
            Method getter = minecraft.getClass().getMethod("getToastManager");
            return (ToastManager)getter.invoke(minecraft);
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Field guiField = minecraft.getClass().getField("gui");
            Object gui = guiField.get(minecraft);
            Method getter = gui.getClass().getMethod("toastManager");
            return (ToastManager)getter.invoke(gui);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
