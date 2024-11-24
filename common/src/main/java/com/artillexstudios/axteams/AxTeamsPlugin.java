package com.artillexstudios.axteams;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.FeatureFlags;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
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
import com.artillexstudios.axteams.listeners.PlayerListener;
import com.artillexstudios.axteams.listeners.TeamPvPListener;
import com.artillexstudios.axteams.utils.FileUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class AxTeamsPlugin extends AxPlugin {
    private static AxTeamsPlugin instance;
    private Metrics metrics;
    private TeamSaver teamSaver;

    public static AxTeamsPlugin instance() {
        return instance;
    }

    @Override
    public void updateFlags() {
        FeatureFlags.PLACEHOLDER_API_HOOK.set(true);
        FeatureFlags.PLACEHOLDER_API_IDENTIFIER.set("axteams");
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
        AsyncUtils.setup(Config.ASYNC_PROCESSOR_POOL_SIZE);

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .setNamespace("axteams")
                .skipReloadDatapacks(true)
        );
    }

    @Override
    public void enable() {
        if (Config.USE_BSTATS) {
            this.metrics = new Metrics(this, 23441);
        }

        DataHandler.setup().thenRun(() -> {
            if (Config.DEBUG) {
                LogUtils.debug("Loaded datahandler!");
            }
        });

        this.teamSaver = new TeamSaver();
        this.teamSaver.start();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new TeamPvPListener(), this);
        AxTeamsCommand.INSTANCE.register();
        CommandAPI.onEnable();
        Guis.loadAll();

        Placeholders.registerTransformer(OfflinePlayer.class, User.class, player -> {
            return AxTeamsAPI.instance().getUserIfLoadedImmediately(player);
        });

        Placeholders.register("team_name", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "Do I know you?";
            }

            Team team = user.team();
            if (team == null) {
                return "No team";
            }

            return team.name();
        }, ParseContext.PLACEHOLDER_API);

        Placeholders.register("team_display_name", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "Do I know you?";
            }

            Team team = user.team();
            if (team == null) {
                return "No team";
            }

            Component first = team.first(TeamValues.TEAM_DISPLAY_NAME);
            if (first == null) {
                return team.name();
            }

            return MiniMessage.miniMessage().serialize(first);
        }, ParseContext.PLACEHOLDER_API);

        Placeholders.register("member_count", ctx -> {
            Team team = ctx.resolve(Team.class);
            if (team == null) {
                return "0";
            }

            return Integer.toString(team.members(true).size());
        });

        Placeholders.register("texture", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "";
            }

            return user.textures();
        });

        Placeholders.register("group_display_name", ctx -> {
            if (ctx.has(Group.class)) {
                Group group = ctx.raw(Group.class);
                if (group == null) {
                    return "---";
                }

                return MiniMessage.miniMessage().serialize(group.displayName());
            }

            User user = ctx.resolve(User.class);
            if (user == null) {
                return "";
            }

            return MiniMessage.miniMessage().serialize(user.group().displayName());
        });

        Placeholders.register("group_name", ctx -> {
            if (ctx.has(Group.class)) {
                Group group = ctx.raw(Group.class);
                if (group == null) {
                    return "---";
                }

                return group.name();
            }

            User user = ctx.resolve(User.class);
            if (user == null) {
                return "---";
            }

            return user.group().name();
        });

        Placeholders.register("player", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "";
            }

            return user.player().getName();
        });

        Placeholders.register("permission", ctx -> {
            Permission permission = ctx.resolve(Permission.class);
            if (permission == null) {
                return "";
            }

            return permission.display();
        });

        Placeholders.register("level", ctx -> {
            if (ctx.has(Team.class)) {
                Team team = ctx.raw(Team.class);
                if (team == null) {
                    return "0";
                }

                return Integer.toString(team.members(true).size());
            }

            User user = ctx.resolve(User.class);
            Team team = user.team();
            if (team == null) {
                return "----"; // TODO: no team from config
            }

            return "level" /*team.first(TeamValues.WARP_LIMIT)*/; // TODO: yes
        });

        Placeholders.register("message", ctx -> {
            return ctx.resolve(String.class);
        });

        FileUtils.copyFromResource("guis");
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
