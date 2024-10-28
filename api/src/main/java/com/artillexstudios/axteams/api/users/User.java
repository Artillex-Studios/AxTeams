package com.artillexstudios.axteams.api.users;

import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Team;
import org.bukkit.OfflinePlayer;
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
}
