package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class Groups {
    private static final Logger log = LoggerFactory.getLogger(Groups.class);
    private static final Groups INSTANCE = new Groups();
    public static List<Supplier<Group>> DEFAULT_GROUPS = new ArrayList<>();
    private com.artillexstudios.axapi.config.Config config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        File file = FileUtils.PLUGIN_DIRECTORY.resolve("groups.yml").toFile();
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        }

        if (this.config != null) {
            this.config.reload();
        } else {
            this.config = new com.artillexstudios.axapi.config.Config(file, AxTeamsPlugin.instance().getResource("groups.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
        }

        refreshValues();
        return true;
    }

    private void refreshValues() {
        if (this.config == null) {
            log.error("Groups were not loaded correctly! Using default values!");
            return;
        }

        boolean hasOwner = false;
        boolean hasDefault = false;
        DEFAULT_GROUPS.clear();
        for (Map<Object, Object> group : this.config.getMapList("groups")) {
            String id = (String) group.get("id");
            if (id == null) {
                LogUtils.warn("No id defined for group!");
                continue;
            }

            String name = (String) group.get("name");
            if (name == null) {
                LogUtils.warn("No name defined for group, defaulting to {}!", id);
                name = id;
            }

            Integer priority = (Integer) group.get("priority");
            if (priority == null) {
                LogUtils.warn("No priority defined for group!");
                continue;
            }

            if (priority == Group.DEFAULT_PRIORITY) {
                if (hasDefault) {
                    LogUtils.warn("Did not load group with id {} due to there already being a default group!", id);
                    continue;
                }

                hasDefault = true;
            }

            if (priority == Group.OWNER_PRIORITY) {
                if (hasOwner) {
                    LogUtils.warn("Did not load group with id {} due to there already being an owner group!", id);
                    continue;
                }

                hasOwner = true;
            }

            List<Permission> permissions = new ArrayList<>();
            List<String> permissionList = (List<String>) group.get("permissions");
            if (permissionList != null) {
                for (String string : permissionList) {
                    Permission permission = Permissions.get(string);
                    if (permission == null) {
                        LogUtils.warn("Could not find permission with id {}!", string);
                        continue;
                    }

                    permissions.add(permission);
                }
            }

            String finalName = name;
            if (Config.debug) {
                LogUtils.debug("Loaded group with id: {}, name: {}, priority: {}, permissions: {}.", id, name, priority, permissions);
            }
            DEFAULT_GROUPS.add(() -> new Group(priority, id, StringUtils.format(finalName), permissions));
        }

        if (!hasOwner) {
            LogUtils.warn("There is no owner group set up!");
        }

        if (!hasDefault) {
            LogUtils.warn("There is no default group set up!");
        }
    }
}
