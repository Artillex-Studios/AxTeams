package com.artillexstudios.axteams;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axteams.command.AxTeamsCommand;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.config.Groups;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.config.Levels;
import com.artillexstudios.axteams.config.Permissions;
import com.artillexstudios.axteams.database.DataHandler;
import com.artillexstudios.axteams.database.DatabaseConnector;
import com.artillexstudios.axteams.database.TeamSaver;
import com.artillexstudios.axteams.guis.Guis;
import com.artillexstudios.axteams.listeners.PlayerChatListener;
import com.artillexstudios.axteams.listeners.PlayerListener;
import com.artillexstudios.axteams.listeners.TeamEnderChestListener;
import com.artillexstudios.axteams.listeners.TeamPvPListener;
import com.artillexstudios.axteams.placeholders.PlaceholderRegistry;
import com.artillexstudios.axteams.utils.FileUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;

public final class AxTeamsPlugin extends AxPlugin {
    private static AxTeamsPlugin instance;
    private Metrics metrics;
    private TeamSaver teamSaver;

    public static AxTeamsPlugin instance() {
        return instance;
    }

    @Override
    public void updateFlags(FeatureFlags flags) {
        flags.PLACEHOLDER_API_HOOK.set(true);
        flags.PLACEHOLDER_API_IDENTIFIER.set("axteams");
    }

    @Override
    public void load() {
        instance = this;

        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        libraryManager.configureFromJSON("libraries.json");

        Permissions.reload();
        Config.reload();
        Language.reload();
        Levels.reload();
        Groups.reload();
        AsyncUtils.setup(Config.asyncProcessorPoolSize);

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .setNamespace("axteams")
                .skipReloadDatapacks(true)
        );
    }

    @Override
    public void enable() {
        if (Config.useBstats) {
            this.metrics = new Metrics(this, 23441);
        }

        DataHandler.setup().thenRun(() -> {
            if (Config.debug) {
                LogUtils.debug("Loaded datahandler!");
            }
        });

        this.teamSaver = new TeamSaver();
        this.teamSaver.start();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new TeamPvPListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new TeamEnderChestListener(), this);
        AxTeamsCommand.INSTANCE.register();
        CommandAPI.onEnable();
        FileUtils.copyFromResource("guis");
        Guis.loadAll();

        PlaceholderRegistry.INSTANCE.register();
    }

    @Override
    public void disable() {
        if (this.metrics != null) {
            this.metrics.shutdown();
        }

        this.teamSaver.stop();
        CommandAPI.onDisable();
        AsyncUtils.stop();
        DatabaseConnector.getInstance().close();
    }
}
