package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public final class Language {
    private static final Logger log = LoggerFactory.getLogger(Language.class);
    private static final Path LANGUAGE_DIRECTORY = FileUtils.PLUGIN_DIRECTORY.resolve("language");
    private static final Language INSTANCE = new Language();
    public static String PREFIX = "<b><gradient:#CB2D3E:#EF473A>AxTeams</gradient></b> ";
    public static String RELOAD_SUCCESS = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white>ms!";
    public static String RELOAD_FAIL = "<#FF0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information! <br>Reload done in: <white><time></white>ms!";
    public static String USER_NOT_LOADED = "<#FF0000>Your userdata has not loaded yet! Please try again in a moment!";
    public static String NOT_IN_TEAM = "Not in team";
    public static String NO_PERMISSION = "No permission";
    public static String NOT_LEADER = "Not leader";
    public static String DISBANDED = "Disbanded";
    public static String ALREADY_IN_TEAM = "In team";
    public static String ALREADY_EXISTS = "Exists";
    public static String TOO_SHORT = "Short";
    public static String TOO_LONG = "Long";
    public static String BLACKLISTED = "Blacklisted";
    public static String NOT_WHITELISTED = "not whitelisted";
    public static String CREATED = "created";
    public static String TEAM_VALUE_NOT_FOUND = "<#FF0000>There is no teamvalue registered with id <id>!";
    public static String TEAM_CHAT_FORMAT = "<#FF0000>Team <white>| <group> <player> <gray>» <white><message>";
    public static String ALLY_CHAT_FORMAT = "<#03BAFC>Ally <white>| <team_display_name> <group> <player> <gray>» <white><message>";
    public static String lastLanguage;
    private com.artillexstudios.axapi.config.Config config = null;

    public static boolean reload() {
        if (Config.DEBUG) {
            LogUtils.debug("Reload called on language!");
        }
        FileUtils.copyFromResource("language");

        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        if (Config.DEBUG) {
            LogUtils.debug("Refreshing language");
        }
        File file = LANGUAGE_DIRECTORY.resolve(Config.LANGUAGE + ".yml").toFile();
        boolean shouldDefault = false;
        if (file.exists()) {
            if (Config.DEBUG) {
                LogUtils.debug("File exists");
            }
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        } else {
            shouldDefault = true;
            file = LANGUAGE_DIRECTORY.resolve("en_US.yml").toFile();
            log.error("No language configuration was found with the name {}! Defaulting to en_US...", Config.LANGUAGE);
        }

        // The user might have changed the config
        if (config != null && lastLanguage != null && lastLanguage.equalsIgnoreCase(Config.LANGUAGE)) {
            if (Config.DEBUG) {
                LogUtils.debug("Config not null");
            }
            config.reload();
        } else {
            lastLanguage = shouldDefault ? "en_US" : Config.LANGUAGE;
            if (Config.DEBUG) {
                LogUtils.debug("Set lastLanguage to {}", lastLanguage);
            }
            InputStream defaults = AxTeamsPlugin.instance().getResource("language/" + lastLanguage + ".yml");
            if (defaults == null) {
                if (Config.DEBUG) {
                    LogUtils.debug("Defaults are null, defaulting to en_US.yml");
                }
                defaults = AxTeamsPlugin.instance().getResource("language/en_US.yml");
            }

            if (Config.DEBUG) {
                LogUtils.debug("Loading config from file {} with defaults {}", file, defaults);
            }
            config = new com.artillexstudios.axapi.config.Config(file, defaults, GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        }

        refreshValues();
        return true;
    }

    private void refreshValues() {
        if (config == null) {
            log.error("Language configuration was not loaded correctly! Using default values!");
            return;
        }

        PREFIX = config.getString("prefix", PREFIX);
        USER_NOT_LOADED = config.getString("error.user-not-loaded", USER_NOT_LOADED);
        TEAM_VALUE_NOT_FOUND = config.getString("error.teamvalue-not-found", TEAM_VALUE_NOT_FOUND);
    }
}
