package com.artillexstudios.axteams.api.teams;

import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.exception.RegistrationFailedException;
import com.artillexstudios.axteams.api.utils.Registry;

import java.util.Collection;

public final class Permissions {
    private static final Registry<String, Permission> registry = new Registry<>();
    public static final Permission ALL = register(new Permission("*", "All"));
    public static final Permission CHANGE_DISPLAY_NAME = register(new Permission("change_display_name", "Change displayname"));
    public static final Permission PVP = register(new Permission("team_pvp", "PvP toggle"));
    public static final Permission WARP_CREATE = register(new Permission("warp_create", "Warp creation"));
    public static final Permission HOME_CREATE = register(new Permission("home_create", "Home creation"));
    public static final Permission INVITE = register(new Permission("invite", "User invite"));
    public static final Permission KICK = register(new Permission("kick", "Kick user"));
    public static final Permission ALLY_SEND = register(new Permission("ally_send", "Send ally request"));
    public static final Permission ALLY_ACCEPT = register(new Permission("ally_accept", "Ally request accept"));
    public static final Permission ALLY_REMOVE = register(new Permission("ally_remove", "Ally remove"));
    public static final Permission GROUP_RENAME = register(new Permission("group_rename", "Rename group"));
    public static final Permission GROUP_DELETE = register(new Permission("group_delete", "Delete group"));
    public static final Permission GROUP_CREATE = register(new Permission("group_create", "Create group"));
    public static final Permission GROUP_PREFIX_CHANGE = register(new Permission("group_prefix_change", "Change group prefix"));
    public static final Permission GROUP_PRIORITY_CHANGE = register(new Permission("group_priority_change", "Change group priority"));
    public static final Permission GROUP_PERMISSIONS_EDIT = register(new Permission("group_permissions_edit", "Edit group permissions"));
    public static final Permission USER_GROUP_MODIFY = register(new Permission("user_group_modify", "Modify the group of a member"));
    public static final Permission ENDER_CHEST_OPEN = register(new Permission("ender_chest_open", "Open the team enderchest"));

    public static Permission register(Permission permission) {
        if (permission.permission().contains(".")) {
            throw new IllegalArgumentException("The permission string can't contain the '.' character! Replace it with something else, like '_'!");
        }

        try {
            registry.register(permission.permission(), permission);
            AxTeamsAPI.instance().registerPermission(permission);
            return permission;
        } catch (RegistrationFailedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Permission get(String permission) {
        try {
            return registry.get(permission);
        } catch (RegistrationFailedException e) {
            return null;
        }
    }

    public static Collection<Permission> values() {
        return registry.values();
    }
}
