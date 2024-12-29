package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.PostProcess;
import com.artillexstudios.axapi.config.annotation.Serializable;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.database.DatabaseType;
import com.artillexstudios.axteams.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Config implements ConfigurationPart {
    private static final Config INSTANCE = new Config();
    public static Database database = new Database();
    public static TeamName teamName = new TeamName();

    @Serializable
    public static class Database {
        @Comment("h2, sqlite or mysql")
        public DatabaseType type = DatabaseType.H2;
        public String address = "127.0.0.1";
        public int port = 3306;
        public String database = "admin";
        public String username = "admin";
        public String password = "admin";
        public Pool pool = new Pool();

        @Serializable
        public static class Pool {
            public int maximumPoolSize = 10;
            public int minimumIdle = 10;
            public int maximumLifetime = 1800000;
            public int keepaliveTime = 0;
            public int connectionTimeout = 5000;

            @PostProcess
            public void postProcess() {
                if (maximumPoolSize < 1) {
                    LogUtils.warn("Maximum database pool size is lower than 1! This is not supported! Defaulting to 1.");
                    maximumPoolSize = 1;
                }
            }
        }
    }

    @Serializable
    public static class TeamName {
        @Comment("If this is enabled, these rules apply to the display-name of the team aswell")
        public boolean applyToDisplayName = false;
        public int minLength = 3;
        @Comment("Set to -1 to disable")
        public int maxLength = 16;
        @Comment("e.g.: Only allow alphanumeric characters; regular expression")
        public List<Pattern> whitelist = new ArrayList<>();
        @Comment("e.g.: The string it contains; regular expression")
        public List<Pattern> blacklist = new ArrayList<>();
    }

    @Comment("""
            What language file should we load from the lang folder?
            You can create your own aswell! We would appreciate if you
            contributed to the plugin by creating a pull request with your translation!
            """)
    public static String language = "en_US";
    @Comment("""
            The cooldown between being able to click in a gui.
            This is in milliseconds, so setting 200 here means that you can
            click every 0.2 seconds.
            """)
    public static int guiActionCooldown = 200;
    @Comment("""
            The default enderchest rows.
            Increasing this will not change the value for
            already existing teams.
            """)
    public static int defaultEnderChestRows = 3;
    @Comment("""
            The default warp limit of a team.
            Increasing this will not change the value for
            already existing teams.
            """)
    public static int defaultWarpLimit = 3;
    @Comment("""
            The default maximal size of a team.
            Increasing this will not change the value for
            already existing teams.
            """)
    public static int defaultTeamSizeLimit = 5;
    @Comment("""
            The default maximal allies of a team.
            Increasing this will not change the value for
            already existing teams.
            """)
    public static int defaultAllyLimit = 5;
    @Comment("""
            How often should changes to teams get saved?
            This setting controls how often teams get saved
            into the database if they have changed.
            """)
    public static int autosaveSeconds = 300;
    @Comment("""
            The pool size of the asynchronous executor
            we use to process some things asynchronously,
            like database queries.
            """)
    public static int asyncProcessorPoolSize = 3;
    @Comment("""
            Allow sending anonymous statistics about the plugin usage.
            We'd appreciate if you kept it enabled, as it helps us track
            how many servers use our plugin
            """)
    public static boolean useBstats = true;
    @Comment("""
            If we should send debug messages in the console
            You shouldn't enable this, unless you want to see what happens in the code
            """)
    public static boolean debug = false;
    @Comment("Do not touch!")
    public static int configVersion = 1;
    private YamlConfiguration config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Config.class)
                    .configVersion(1, "config-version")
                    .withDefaults(AxTeamsPlugin.instance().getResource("config.yml"))
                    .withDumperOptions(options -> {
                        options.setPrettyFlow(true);
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }

    @PostProcess
    public static void postProcess() {
        if (autosaveSeconds <= 0) {
            LogUtils.warn("Autosave frequency is set too low! Defaulting to 15 seconds!");
            autosaveSeconds = 15;
        }
    }
}
