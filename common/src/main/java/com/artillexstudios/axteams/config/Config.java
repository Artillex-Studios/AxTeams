package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.database.DatabaseType;
import com.artillexstudios.axteams.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static final Config INSTANCE = new Config();
    public static String DATABASE_ADDRESS = "127.0.0.1";
    public static int DATABASE_PORT = 3306;
    public static String DATABASE_DATABASE = "admin";
    public static String DATABASE_USERNAME = "admin";
    public static String DATABASE_PASSWORD = "admin";
    public static int DATABASE_MAXIMUM_POOL_SIZE = 10;
    public static int DATABASE_MINIMUM_IDLE = 10;
    public static int DATABASE_MAXIMUM_LIFETIME = 1800000;
    public static int DATABASE_KEEPALIVE_TIME = 0;
    public static int DATABASE_CONNECTION_TIMEOUT = 5000;
    public static DatabaseType DATABASE_TYPE = DatabaseType.H2;
    public static int TEAM_NAME_MIN_LENGTH = 3;
    public static int TEAM_NAME_MAX_LENGTH = 16;
    public static int GUI_ACTION_COOLDOWN = 200;
    public static List<Pattern> TEAM_NAME_WHITELIST = new ArrayList<>();
    public static List<Pattern> TEAM_NAME_BLACKLIST = new ArrayList<>();
    public static int AUTOSAVE_SECONDS = 300;
    public static int ASYNC_PROCESSOR_POOL_SIZE = 3;
    public static String LANGUAGE = "en_US";
    public static boolean USE_BSTATS = true;
    public static boolean DEBUG = false;
    private com.artillexstudios.axapi.config.Config config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        File file = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml").toFile();
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        }

        if (config != null) {
            config.reload();
        } else {
            config = new com.artillexstudios.axapi.config.Config(file, AxTeamsPlugin.instance().getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        }

        refreshValues();
        return true;
    }

    private void refreshValues() {
        if (config == null) {
            log.error("Config was not loaded correctly! Using default values!");
            return;
        }

        DATABASE_TYPE = DatabaseType.parse(config.getString("database.type", DATABASE_TYPE.name()));
        DATABASE_ADDRESS = config.getString("database.address", DATABASE_ADDRESS);
        DATABASE_PORT = config.getInt("database.port", DATABASE_PORT);
        DATABASE_DATABASE = config.getString("database.database", DATABASE_DATABASE);
        DATABASE_USERNAME = config.getString("database.username", DATABASE_USERNAME);
        DATABASE_PASSWORD = config.getString("database.password", DATABASE_PASSWORD);
        DATABASE_MAXIMUM_POOL_SIZE = config.getInt("database.pool.maximum-pool-size", DATABASE_MAXIMUM_POOL_SIZE);
        DATABASE_MINIMUM_IDLE = config.getInt("database.pool.minimum-idle", DATABASE_MINIMUM_IDLE);
        DATABASE_MAXIMUM_LIFETIME = config.getInt("database.pool.maximum-lifetime", DATABASE_MAXIMUM_LIFETIME);
        DATABASE_KEEPALIVE_TIME = config.getInt("database.pool.keepalive-time", DATABASE_KEEPALIVE_TIME);
        DATABASE_CONNECTION_TIMEOUT = config.getInt("database.pool.connection-timeout", DATABASE_CONNECTION_TIMEOUT);
        TEAM_NAME_MIN_LENGTH = config.getInt("team-name.min-length", TEAM_NAME_MIN_LENGTH);
        TEAM_NAME_MAX_LENGTH = config.getInt("team-name.max-length", TEAM_NAME_MAX_LENGTH);
        TEAM_NAME_WHITELIST.clear();
        TEAM_NAME_WHITELIST.addAll(config.getStringList("team-name.whitelist", List.of("^[a-zA-Z0-9_]*$")).stream().map(Pattern::compile).toList());
        TEAM_NAME_BLACKLIST.clear();
        TEAM_NAME_BLACKLIST.addAll(config.getStringList("team-name.blacklist", List.of("badword")).stream().map(Pattern::compile).toList());
        AUTOSAVE_SECONDS = config.getInt("autosave-seconds", AUTOSAVE_SECONDS);
        ASYNC_PROCESSOR_POOL_SIZE = config.getInt("async-processor-pool-size", ASYNC_PROCESSOR_POOL_SIZE);
        LANGUAGE = config.getString("language", LANGUAGE);
        USE_BSTATS = config.getBoolean("use-bstats", USE_BSTATS);
        DEBUG = config.getBoolean("debug", DEBUG);

        this.validate();
    }

    private void validate() {
        if (Config.AUTOSAVE_SECONDS <= 0) {
            LogUtils.warn("Autosave frequency is set too low! Defaulting to 15 seconds!");
            AUTOSAVE_SECONDS = 15;
        }

        if (Config.AUTOSAVE_SECONDS <= 5) {
            LogUtils.warn("It is not recommended to set autosave-seconds to <= 5, as this might degrade performance!");
        }

        if (Config.DATABASE_MAXIMUM_POOL_SIZE < 1) {
            LogUtils.warn("Maximum database pool size is lower than 1! This is not supported! Defaulting to 1.");
            DATABASE_MAXIMUM_POOL_SIZE = 1;
        }
    }
}
