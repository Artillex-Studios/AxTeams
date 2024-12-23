package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.PostProcess;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.database.DatabaseType;
import com.artillexstudios.axteams.utils.FileUtils;
import org.yaml.snakeyaml.DumperOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Config implements ConfigurationPart {
    private static final Config INSTANCE = new Config();

    public static class Database implements ConfigurationPart {
        public static DatabaseType type = DatabaseType.H2;
        public static String address = "127.0.0.1";
        public static int port = 3306;
        public static String database = "admin";
        public static String username = "admin";
        public static String password = "admin";

        public static class Pool implements ConfigurationPart {
            public static int maximumPoolSize = 10;
            public static int minimumIdle = 10;
            public static int maximumLifetime = 1800000;
            public static int keepaliveTime = 0;
            public static int connectionTimeout = 5000;

            @PostProcess
            public static void postProcess() {
                if (maximumPoolSize < 1) {
                    LogUtils.warn("Maximum database pool size is lower than 1! This is not supported! Defaulting to 1.");
                    maximumPoolSize = 1;
                }
            }
        }
    }

    public static class TeamName implements ConfigurationPart {
        public static boolean applyToDisplayName = false;
        public static int minLength = 3;
        public static int maxLength = 16;
        public static List<Pattern> whitelist = new ArrayList<>();
        public static List<Pattern> blacklist = new ArrayList<>();
    }

    public static String language = "en_US";
    public static int guiActionCooldown = 200;
    public static int defaultWarpLimit = 3;
    public static int autosaveSeconds = 300;
    public static int asyncProcessorPoolSize = 3;
    public static boolean useBstats = true;
    public static boolean debug = false;
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
        this.config.save();
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
