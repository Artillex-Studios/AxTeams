package com.artillexstudios.axteams.utils;

import com.artillexstudios.axapi.utils.Version;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.view.AnvilView;

@SuppressWarnings("removal")
public final class AnvilInputUtils {

    public static String getRenameText(InventoryClickEvent event) {
        if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21)) {
            return ((AnvilView) event.getView()).getRenameText();
        } else {
            return ((AnvilInventory) event.getInventory()).getRenameText();
        }
    }
}
