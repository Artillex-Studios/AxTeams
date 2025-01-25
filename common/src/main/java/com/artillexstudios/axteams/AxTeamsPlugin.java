package com.artillexstudios.axteams;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.metrics.AxMetrics;
import com.artillexstudios.axapi.reflection.ClassUtils;
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
import com.artillexstudios.axteams.placeholders.PlaceholderAPIHook;
import com.artillexstudios.axteams.placeholders.PlaceholderRegistry;
import com.artillexstudios.axteams.utils.FileUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AxTeamsPlugin extends AxPlugin {
    private static AxTeamsPlugin instance;
    private final JavaPlugin plugin;
    private AxMetrics metrics;
    private TeamSaver teamSaver;

    public AxTeamsPlugin(JavaPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public static AxTeamsPlugin instance() {
        return instance;
    }

    @Override
    public void updateFlags(FeatureFlags flags) {
        flags.PLACEHOLDER_API_HOOK.set(true);
        flags.DEBUG.set(true);
        flags.PLACEHOLDER_API_IDENTIFIER.set("axteams");
    }

    @Override
    public void load() {
        instance = this;

//        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this.plugin);
//        libraryManager.configureFromJSON("libraries.json");

        Permissions.reload();
        Config.reload();
        Language.reload();
        Levels.reload();
        Groups.reload();
        AsyncUtils.setup(Config.asyncProcessorPoolSize);

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this.plugin)
                .setNamespace("axteams")
                .skipReloadDatapacks(true)
        );
    }

    @Override
    public void enable() {
        if (Config.useBstats) {
            this.metrics = new AxMetrics(2);
            this.metrics.start();
        }

        DataHandler.setup().thenRun(() -> {
            if (Config.debug) {
                LogUtils.debug("Loaded datahandler!");
            }
        });

        this.teamSaver = new TeamSaver();
        this.teamSaver.start();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this.plugin);
        Bukkit.getPluginManager().registerEvents(new TeamPvPListener(), this.plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(), this.plugin);
        Bukkit.getPluginManager().registerEvents(new TeamEnderChestListener(), this.plugin);
        AxTeamsCommand.INSTANCE.register();
        CommandAPI.onEnable();
        FileUtils.copyFromResource("guis");
        Guis.loadAll();

        PlaceholderRegistry.INSTANCE.register();
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            LogUtils.info("PlaceholderAPIHook register!");
            new PlaceholderAPIHook().register();
        }
        //        PacketEvents.INSTANCE.addListener(new PacketListener() {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//                if (ClientboundEntitySoundWrapper.check(event)) {
//                    System.out.println("RECEIVED SOUND EFFECT!");
//                }
//            }
//
//            @Override
//            public void onPacketReceive(PacketEvent event) {
//
//            }
//        });
    }

    @Override
    public void disable() {
//        if (this.metrics != null) {
//            this.metrics.cancel();
//        }

        this.teamSaver.stop();
        CommandAPI.onDisable();
        AsyncUtils.stop();
        DatabaseConnector.getInstance().close();
    }
}
