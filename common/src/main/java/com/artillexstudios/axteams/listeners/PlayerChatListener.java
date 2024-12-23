package com.artillexstudios.axteams.listeners;

import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.api.users.settings.UserSettings;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.config.Language;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;

public final class PlayerChatListener implements Listener {

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(event.getPlayer());
        if (user == null) {
            return;
        }

        Team team = user.team();
        if (user.settingsRepository().read(UserSettings.TEAM_CHAT_TOGGLED)) {
            if (team == null) {
                MessageUtils.sendMessage(player, Language.PREFIX, "Team chat disabled");
                UserSettings.TEAM_CHAT_TOGGLED.write(user, false);
                return;
            }

            Map<String, String> placeholders = Placeholders.asMap(Context.builder(ParseContext.INTERNAL).add(String.class, event.getMessage()).add(User.class, user));
            if (Config.debug) {
                LogUtils.debug("Placeholders in message send: {}", placeholders);
            }

            event.setCancelled(true);
            team.message(StringUtils.format(Language.TEAM_CHAT_FORMAT, placeholders));
        } else if (user.settingsRepository().read(UserSettings.ALLY_CHAT_TOGGLED)) {
            if (team == null) {
                MessageUtils.sendMessage(player, Language.PREFIX, "Ally chat disabled");
                UserSettings.ALLY_CHAT_TOGGLED.write(user, false);
                return;
            }

            Component message = StringUtils.format(Language.ALLY_CHAT_FORMAT, Placeholders.asMap(Context.builder(ParseContext.INTERNAL).add(String.class, event.getMessage()).add(User.class, user)));
            event.setCancelled(true);
            team.message(message);
            for (Integer value : team.values(TeamValues.ALLIES)) {
                Team ally = AxTeamsAPI.instance().getTeamIfLoadedImmediately(new TeamID(value));
                if (ally != null) {
                    ally.message(message);
                }
            }
        }
    }
}
