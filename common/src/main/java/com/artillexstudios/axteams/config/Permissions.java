package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Permissions implements ConfigurationPart {
    private static final Permissions INSTANCE = new Permissions();
    public static Map<String, Map<String, Object>> permissions = new LinkedHashMap<>();
    public static int configVersion = 1;
    private YamlConfiguration config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    public static void modify(Permission permission) {
        if (Config.debug) {
            LogUtils.debug("Modifying permission {}", permission.permission());
        }
        String display = INSTANCE.config.get("permissions.%s.display".formatted(permission.permission()), String.class);
        if (display == null) {
            display = permission.display();
            INSTANCE.config.set("permissions.%s.display".formatted(permission.permission()), display);
            INSTANCE.config.save();
        }

        permission.display(display);
    }

    private boolean refreshConfig() {
        if (Config.debug) {
            LogUtils.debug("Reloading permissions.yml!");
        }
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("permissions.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Permissions.class)
                    .configVersion(1, "config-version")
                    .withDefaults(AxTeamsPlugin.instance().getResource("permissions.yml"))
                    .build();
        }

        this.config.load();
        return true;
    }
}
