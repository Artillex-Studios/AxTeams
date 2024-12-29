package com.artillexstudios.axteams.listeners;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableByteArray;
import com.artillexstudios.axteams.teams.TeamInventoryHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class TeamEnderChestListener implements Listener {

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof TeamInventoryHolder holder) {
            holder.team().add(TeamValues.ENDER_CHEST, new IdentifiableByteArray(Serializers.ITEM_ARRAY.serialize(event.getInventory().getContents())));
            holder.team().markUnsaved();
        }
    }
}
