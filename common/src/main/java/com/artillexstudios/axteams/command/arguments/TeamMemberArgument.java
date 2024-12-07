package com.artillexstudios.axteams.command.arguments;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.users.Users;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

public final class TeamMemberArgument {

    public static Argument<User> teamMember(String name) {
        return new CustomArgument<>(new StringArgument(name), info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                throw CustomArgument.CustomArgumentException.fromString("Sender should be a player!");
            }

            for (com.artillexstudios.axteams.api.users.User member : team.members()) {
                String playerName = member.player().getName();
                if (playerName == null) {
                    continue;
                }

                if (playerName.equalsIgnoreCase(info.input())) {
                    return member;
                }
            }

            throw CustomArgument.CustomArgumentException.fromString(StringUtils.formatToString(Language.PREFIX + Language.UNKNOWN_MEMBER, Placeholder.parsed("name", info.input())));
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                return List.of();
            }

            return team.members().stream().map(user -> user.player().getName()).toList();
        }));
    }

    public static Argument<User> membersWithOwner(String name) {
        return new CustomArgument<>(new StringArgument(name), info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                throw CustomArgument.CustomArgumentException.fromString("You are not in a team!");
            }

            for (com.artillexstudios.axteams.api.users.User member : team.members(true)) {
                String playerName = member.player().getName();
                if (playerName == null) {
                    continue;
                }

                if (playerName.equalsIgnoreCase(info.input())) {
                    return member;
                }
            }

            throw CustomArgument.CustomArgumentException.fromString(StringUtils.formatToString(Language.PREFIX + Language.UNKNOWN_MEMBER, Placeholder.parsed("name", info.input())));
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
            if (team == null) {
                return List.of();
            }

            return team.members(true).stream().map(user -> user.player().getName()).toList();
        }));
    }
}
