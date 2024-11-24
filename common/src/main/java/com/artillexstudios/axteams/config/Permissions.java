package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.utils.FileUtils;

import java.io.File;

public final class Permissions {
    private static final Permissions INSTANCE = new Permissions();
    private com.artillexstudios.axapi.config.Config config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    public static void modify(Permission permission) {
        if (Config.DEBUG) {
            LogUtils.debug("Modifying permission {}", permission.permission());
        }
        String display = INSTANCE.config.getString("permissions.%s.display".formatted(permission.permission()));
        if (display == null) {
            display = permission.display();
            INSTANCE.config.set("permissions.%s.display".formatted(permission.permission()), display);
            INSTANCE.config.save();
        }

        permission.display(display);
    }

    private boolean refreshConfig() {
        if (Config.DEBUG) {
            LogUtils.debug("Reloading permissions.yml!");
        }
        File file = FileUtils.PLUGIN_DIRECTORY.resolve("permissions.yml").toFile();
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        }

        if (this.config != null) {
            this.config.reload();
        } else {
            this.config = new com.artillexstudios.axapi.config.Config(file, AxTeamsPlugin.instance().getResource("permissions.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        }

        return true;
    }
}
