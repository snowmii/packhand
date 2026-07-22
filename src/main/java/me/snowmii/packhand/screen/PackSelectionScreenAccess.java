package me.snowmii.packhand.screen;

import net.minecraft.client.gui.screens.packs.TransferableSelectionList;

public interface PackSelectionScreenAccess {
    boolean packhand$isResourcePackScreen();

    TransferableSelectionList packhand$getAvailablePackList();

    TransferableSelectionList packhand$getSelectedPackList();
}
