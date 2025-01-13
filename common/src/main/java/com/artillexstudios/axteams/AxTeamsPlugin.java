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
        LogUtils.info("Loading AxTeamsPlugin...");
        instance = this;

        try {
            BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
            libraryManager.configureFromJSON("libraries.json");
            LogUtils.info("Libraries loaded successfully.");
        } catch (Exception e) {
            LogUtils.error("Failed to load libraries.", e);
        }

        try {
            Permissions.reload();
            Config.reload();
            Language.reload();
            Levels.reload();
            Groups.reload();
            LogUtils.info("Configurations loaded successfully.");
        } catch (Exception e) {
            LogUtils.error("Failed to load configurations.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            AsyncUtils.setup(Config.asyncProcessorPoolSize);
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                    .setNamespace("axteams")
                    .skipReloadDatapacks(true));
            LogUtils.info("Async utilities and CommandAPI initialized.");
        } catch (Exception e) {
            LogUtils.error("Error initializing CommandAPI or Async utilities.", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void enable() {
        LogUtils.info("Enabling AxTeamsPlugin...");

        if (Config.useBstats) {
            try {
                this.metrics = new Metrics(this, 23441);
                LogUtils.info("bStats metrics initialized.");
            } catch (Exception e) {
                LogUtils.warn("Failed to initialize bStats metrics.", e);
            }
        }

        try {
            DataHandler.setup().thenRun(() -> LogUtils.debug("DataHandler setup completed."));
        } catch (Exception e) {
            LogUtils.error("Error setting up DataHandler.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.teamSaver = new TeamSaver();
        try {
            this.teamSaver.start();
            LogUtils.info("TeamSaver started.");
        } catch (Exception e) {
            LogUtils.error("Failed to start TeamSaver.", e);
        }

        try {
            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
            Bukkit.getPluginManager().registerEvents(new TeamPvPListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerChatListener(), this);
            Bukkit.getPluginManager().registerEvents(new TeamEnderChestListener(), this);
            LogUtils.info("Event listeners registered.");
        } catch (Exception e) {
            LogUtils.error("Error registering event listeners.", e);
        }

        try {
            AxTeamsCommand.INSTANCE.register();
            CommandAPI.onEnable();
            FileUtils.copyFromResource("guis");
            Guis.loadAll();
            PlaceholderRegistry.INSTANCE.register();
            LogUtils.info("AxTeamsPlugin enabled successfully.");
        } catch (Exception e) {
            LogUtils.error("Error during plugin enable.", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void disable() {
        LogUtils.info("Disabling AxTeamsPlugin...");

        if (this.metrics != null) {
            try {
                this.metrics.shutdown();
                LogUtils.info("bStats metrics shut down.");
            } catch (Exception e) {
                LogUtils.warn("Failed to shut down bStats metrics.", e);
            }
        }

        try {
            if (this.teamSaver != null) {
                this.teamSaver.stop();
                LogUtils.info("TeamSaver stopped.");
            }
        } catch (Exception e) {
            LogUtils.warn("Error stopping TeamSaver.", e);
        }

        try {
            CommandAPI.onDisable();
            LogUtils.info("CommandAPI disabled.");
        } catch (Exception e) {
            LogUtils.warn("Error disabling CommandAPI.", e);
        }

        try {
            AsyncUtils.stop();
            LogUtils.info("Async utilities stopped.");
        } catch (Exception e) {
            LogUtils.warn("Error stopping Async utilities.", e);
        }

        try {
            DatabaseConnector.getInstance().close();
            LogUtils.info("Database connection closed.");
        } catch (Exception e) {
            LogUtils.warn("Error closing database connection.", e);
        }

        LogUtils.info("AxTeamsPlugin disabled successfully.");
    }
}
