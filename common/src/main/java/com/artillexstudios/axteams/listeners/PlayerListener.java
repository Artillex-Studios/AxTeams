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
                if (Config.DEBUG) {
                    LogUtils.debug("Loaded user!");
                }

                // TODO: Send message that there are ally requests

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

        Team team = user.team();
        if (team == null) {
            return;
        }

        if (!team.hasOnline()) {
            Teams.disconnect(team.id());
        }
    }
}
