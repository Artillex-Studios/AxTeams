package com.artillexstudios.axteams;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.metrics.AxMetrics;
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
import org.bukkit.Bukkit;
import revxrsal.zapper.Dependency;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;

public final class AxTeamsPlugin extends AxPlugin {
    private static AxTeamsPlugin instance;
    private AxMetrics metrics;
    private TeamSaver teamSaver;

    public static AxTeamsPlugin instance() {
        return instance;
    }

    @Override
    public void updateFlags(FeatureFlags flags) {
        flags.PLACEHOLDER_API_HOOK.set(true);
//        flags.DEBUG.set(true);
        flags.PLACEHOLDER_API_IDENTIFIER.set("axteams");
    }

    @Override
    public void dependencies(DependencyManager manager) {
        manager.repository(Repository.maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/"));
        manager.repository(Repository.maven("https://repo.codemc.org/repository/maven-public/"));
        manager.repository(Repository.jitpack());

        manager.dependency(new Dependency("dev{}jorel".replace("{}", "."), "commandapi-bukkit-shade", "9.7.0", null, true));
        manager.dependency("dev{}triumphteam:triumph-gui:3.1.10".replace("{}", "."));
        manager.dependency("com{}h2database:h2:2.3.232".replace("{}", "."));
        manager.dependency("com{}zaxxer:HikariCP:5.1.0".replace("{}", "."));
        manager.dependency("org{}jooq:jooq:3.19.10".replace("{}", "."));
        manager.relocate(new Relocation("org{}h2".replace("{}", "."), "com.artillexstudios.axteams.libs.h2"));
        manager.relocate(new Relocation("dev{}jorel{}commandapi".replace("{}", "."), "com.artillexstudios.axteams.libs.commandapi"));
        manager.relocate(new Relocation("dev{}triumphteam{}gui".replace("{}", "."), "com.artillexstudios.axteams.libs.triumphgui"));
        manager.relocate(new Relocation("org{}jooq".replace("{}", "."), "com.artillexstudios.axteams.libs.jooq"));
        manager.relocate(new Relocation("com{}zaxxer".replace("{}", "."), "com.artillexstudios.axteams.libs.hikaricp"));
    }

    @Override
    public void load() {
        instance = this;

        Permissions.reload();
        Config.reload();
        Language.reload();
        Levels.reload();
        Groups.reload();
        AsyncUtils.setup(Config.asyncProcessorPoolSize);

        AxTeamsCommand.INSTANCE.load(this);
    }

    @Override
    public void enable() {
        if (Config.useBstats) {
            this.metrics = new AxMetrics(this, 2);
            this.metrics.start();
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
        AxTeamsCommand.INSTANCE.enable();
        FileUtils.copyFromResource("guis");
        Guis.loadAll();

        PlaceholderRegistry.INSTANCE.register();
    }

    @Override
    public void disable() {
        if (this.metrics != null) {
            this.metrics.cancel();
        }

        this.teamSaver.stop();
        AxTeamsCommand.INSTANCE.disable();
        AsyncUtils.stop();
        DatabaseConnector.getInstance().close();
    }
}
