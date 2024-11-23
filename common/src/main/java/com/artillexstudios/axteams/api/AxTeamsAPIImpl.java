package com.artillexstudios.axteams.api;

import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Permissions;
import com.artillexstudios.axteams.database.DatabaseConnector;
import com.artillexstudios.axteams.teams.Teams;
import com.artillexstudios.axteams.users.Users;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class AxTeamsAPIImpl implements AxTeamsAPI {

    @Override
    public CompletableFuture<Team> team(TeamID teamID) {
        return Teams.getTeam(teamID, LoadContext.TEMPORARY);
    }

    @Nullable
    @Override
    public Team getTeamIfLoadedImmediately(TeamID teamId) {
        return Teams.getTeamIfLoadedImmediately(teamId);
    }

    @Override
    public User getUserIfLoadedImmediately(UUID uuid) {
        return Users.getUserIfLoadedImmediately(uuid);
    }

    @Override
    public CompletableFuture<User> user(UUID uuid) {
        return Users.getUser(uuid, LoadContext.TEMPORARY);
    }

    @Override
    public TeamID getTeamID(String name) {
        return Teams.byName(name);
    }

    @Override
    public DSLContext context() {
        return DatabaseConnector.getInstance().context();
    }

    @Override
    public void registerPermission(Permission permission) {
        Permissions.modify(permission);
    }
}
