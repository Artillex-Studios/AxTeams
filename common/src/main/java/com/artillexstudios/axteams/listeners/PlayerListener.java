package com.artillexstudios.axteams.listeners;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.exception.UserAlreadyLoadedException;
import com.artillexstudios.axteams.teams.Teams;
import com.artillexstudios.axteams.users.Users;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        try {
            Users.loadUser(event.getPlayer().getUniqueId()).thenAccept(user -> {
                ((com.artillexstudios.axteams.users.User) user).onlinePlayer(event.getPlayer());
                if (Config.debug) {
                    LogUtils.debug("Loaded user!");
                }
            });
        } catch (UserAlreadyLoadedException ignored) {
            LogUtils.error("Could not load user, as the user is already loaded!");
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        User user = Users.disconnect(event.getPlayer().getUniqueId());
        if (user == null) {
            return;
        }

        ((com.artillexstudios.axteams.users.User) user).onlinePlayer(null);

        Team team = user.team();
        if (team == null) {
            return;
        }

        if (!team.hasOnline()) {
            if (Config.debug) {
                LogUtils.debug("Disconnecting team!");
            }
            Teams.disconnect(team.id());
        }
    }
}
