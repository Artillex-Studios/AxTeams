package com.artillexstudios.axteams.api.users;

import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.api.teams.Team;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface User {

    int id();

    void team(Team team);

    @Nullable
    Team team();

    OfflinePlayer player();

    long lastOnline();

    String textures();

    void group(Group group);

    Group group();

    void markUnsaved();

    void message(Component message);

    Player onlinePlayer();

    String name();

    default boolean hasPermission(Permission permission) {
        Team team = this.team();
        if (team == null) {
            return false;
        }

        return team.hasPermission(this, permission);
    }

    default boolean hasPermission(Permission permission, User other) {
        return this.hasPermission(permission, other.group());
    }

    default boolean hasPermission(Permission permission, Group group) {
        Team team = this.team();
        if (team == null) {
            return false;
        }

        return team.hasPermission(this, group, permission);
    }
}
