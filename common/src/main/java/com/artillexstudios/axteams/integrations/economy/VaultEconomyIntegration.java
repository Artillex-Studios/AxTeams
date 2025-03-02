package com.artillexstudios.axteams.integrations.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultEconomyIntegration implements EconomyIntegration {
    private final Economy economy;

    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        } else {
            throw new IllegalStateException("Vault is not present on this server!");
        }
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public double fetch(OfflinePlayer player) {
        return economy.getBalance(player);
    }
}
