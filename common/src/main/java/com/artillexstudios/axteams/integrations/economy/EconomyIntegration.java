package com.artillexstudios.axteams.integrations.economy;

import org.bukkit.OfflinePlayer;

public interface EconomyIntegration {

    boolean deposit(OfflinePlayer player, double amount);

    double fetch(OfflinePlayer player);
}
