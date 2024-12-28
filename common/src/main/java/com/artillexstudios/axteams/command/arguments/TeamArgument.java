package com.artillexstudios.axteams.command.arguments;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.teams.Teams;
import com.artillexstudios.axteams.users.Users;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TeamArgument {

    public static Argument<TeamID> team(String valueName) {
        return new CustomArgument<>(new StringArgument(valueName), info -> {
            TeamID id = Teams.byName(info.input());
            if (id == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.prefix + Language.error.notFound, Placeholder.parsed("name", info.input())));
            }

            return id;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> Teams.names()));
    }

    public static Argument<TeamID> ally(String valueName) {
        return team(valueName).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                return List.of();
            }

            List<Integer> allies = team.values(TeamValues.ALLIES);
            List<String> teamNames = new ArrayList<>(allies.size());
            for (Integer ally : allies) {
                teamNames.add(AxTeamsAPI.instance().team(new TeamID(ally)).join().name());
            }

            return teamNames;
        }));
    }

    public static Argument<TeamID> invited(String valueName) {
        return team(valueName).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                return List.of();
            }

            List<TeamID> invited = team.allyRequest();
            List<String> teamNames = new ArrayList<>(invited.size());
            for (TeamID invite : invited) {
                teamNames.add(AxTeamsAPI.instance().team(invite).join().name());
            }

            return teamNames;
        }));
    }
}
