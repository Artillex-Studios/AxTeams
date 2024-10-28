package com.artillexstudios.axteams.command.arguments;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.teams.values.identifiables.Warp;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.users.Users;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

public final class WarpArgument {

    public static Argument<Warp> senderWarp(String warpName) {
        return new CustomArgument<>(new StringArgument(warpName), info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                throw CustomArgument.CustomArgumentException.fromString("Sender should be a player!");
            }

            Warp warp = null;
            List<Warp> warps = team.values(TeamValues.WARPS);
            if (warps != null) {
                for (Warp w : warps) {
                    if (!w.name().equalsIgnoreCase(info.input())) {
                        continue;
                    }

                    warp = w;
                    break;
                }
            }

            if (warp == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX, Placeholder.parsed("name", info.input())));
            }

            return warp;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                return List.of();
            }

            return team.rawValues(TeamValues.WARPS).stream().map(Warp::name).toList();
        }));
    }
}
