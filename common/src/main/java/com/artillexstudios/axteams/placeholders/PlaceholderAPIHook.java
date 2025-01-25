package com.artillexstudios.axteams.placeholders;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.LogUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public final class PlaceholderAPIHook extends PlaceholderExpansion {
    private static final JavaPlugin plugin = AxPlugin.getPlugin();

    @Override
    public boolean persist() {
        return true;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        String identifier = AxPlugin.flags().PLACEHOLDER_API_IDENTIFIER.get();
        if (identifier.isBlank()) {
            String pluginName = plugin.getName().toLowerCase(Locale.ENGLISH);
            LogUtils.error("PlaceholderAPI identifier is not set up! Please set it! Defaulting to {}", pluginName);
            return pluginName;
        }

        return identifier;
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Artillex-Studios";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return Placeholders.parse(params, Context.builder(ParseContext.PLACEHOLDER_API, ResolutionType.ONLINE)
                .add(Player.class, player)
        );
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return Placeholders.parse(params, Context.builder(ParseContext.PLACEHOLDER_API, ResolutionType.OFFLINE)
                .add(OfflinePlayer.class, player)
        );
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return Placeholders.placeholders(ParseContext.PLACEHOLDER_API);
    }
}
