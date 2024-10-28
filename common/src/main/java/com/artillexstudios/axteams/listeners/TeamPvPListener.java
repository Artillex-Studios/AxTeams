package com.artillexstudios.axteams.listeners;

import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class TeamPvPListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof Player damaged) {
            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(player);
            User damagedUser = AxTeamsAPI.instance().getUserIfLoadedImmediately(damaged);

            Team userTeam = user.team();
            Team damagedTeam = damagedUser.team();
            if (userTeam == null || damagedTeam == null) {
                return;
            }

            if (!userTeam.equals(damagedTeam)) {
                return;
            }

            Boolean pvp = userTeam.first(TeamValues.PVP);
            if (pvp == null) {
                return;
            }

            event.setCancelled(!pvp);
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player && event.getEntity() instanceof Player damaged) {
            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(player);
            User damagedUser = AxTeamsAPI.instance().getUserIfLoadedImmediately(damaged);

            Team userTeam = user.team();
            Team damagedTeam = damagedUser.team();
            if (userTeam == null || damagedTeam == null) {
                return;
            }

            if (!userTeam.equals(damagedTeam)) {
                return;
            }

            Boolean pvp = userTeam.first(TeamValues.PVP);
            if (pvp == null) {
                return;
            }

            event.setCancelled(!pvp);
        }
    }
}
