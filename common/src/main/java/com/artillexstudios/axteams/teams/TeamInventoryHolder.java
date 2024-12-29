package com.artillexstudios.axteams.teams;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public record TeamInventoryHolder(Team team) implements InventoryHolder {

    @NotNull
    @Override
    public Inventory getInventory() {
        return null;
    }
}
