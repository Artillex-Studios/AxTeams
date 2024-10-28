package com.artillexstudios.axteams.api;

import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.users.User;
import net.kyori.adventure.util.Services;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AxTeamsAPI {

    default CompletableFuture<Team> teamOf(OfflinePlayer offlinePlayer) {
        return this.teamOf(offlinePlayer.getUniqueId());
    }

    @Nullable
    default Team teamOf(Player player) {
        User user = this.getUserIfLoadedImmediately(player.getUniqueId());
        if (user == null) {
            return null;
        }

        return user.team();
    }

    CompletableFuture<Team> team(TeamID uuid);

    default CompletableFuture<Team> teamOf(UUID uuid) {
        CompletableFuture<Team> teamFuture = new CompletableFuture<>();
        this.user(uuid).thenAccept(user -> teamFuture.complete(user.team()));
        return teamFuture;
    }

    @Nullable
    Team getTeamIfLoadedImmediately(TeamID teamId);

    default User getUserIfLoadedImmediately(OfflinePlayer offlinePlayer) {
        return this.getUserIfLoadedImmediately(offlinePlayer.getUniqueId());
    }

    User getUserIfLoadedImmediately(UUID uuid);

    CompletableFuture<User> user(UUID uuid);

    TeamID getTeamID(String name);

    DSLContext context();

    static AxTeamsAPI instance() {
        return Holder.INSTANCE;
    }

    class Holder {
        private static final AxTeamsAPI INSTANCE = Services.service(AxTeamsAPI.class).orElseThrow();
    }
}
