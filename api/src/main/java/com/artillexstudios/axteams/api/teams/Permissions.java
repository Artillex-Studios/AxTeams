package com.artillexstudios.axteams.api.teams;

import com.artillexstudios.axteams.api.exception.RegistrationFailedException;
import com.artillexstudios.axteams.api.utils.Registry;

public final class Permissions {
    private static final Registry<String, Permission> registry = new Registry<>();
    public static final Permission ALL = register(new Permission("*"));
    public static final Permission CHANGE_DISPLAY_NAME = register(new Permission("change_display_name"));
    public static final Permission PVP = register(new Permission("team_pvp"));
    public static final Permission WARP_CREATE = register(new Permission("warp_create"));
    public static final Permission HOME_CREATE = register(new Permission("home_create"));
    public static final Permission INVITE = register(new Permission("invite"));
    public static final Permission KICK = register(new Permission("kick"));
    public static final Permission ALLY_SEND = register(new Permission("ally_send"));
    public static final Permission ALLY_ACCEPT = register(new Permission("ally_accept"));

    public static Permission register(Permission permission) {
        try {
            registry.register(permission.permission(), permission);
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
}
