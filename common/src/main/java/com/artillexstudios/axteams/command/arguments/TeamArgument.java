package com.artillexstudios.axteams.command.arguments;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.teams.Teams;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public final class TeamArgument {

    public static Argument<TeamID> team(String valueName) {
        return new CustomArgument<>(new StringArgument(valueName), info -> {
            TeamID id = Teams.byName(info.input());
            if (id == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX + Language.TEAM_VALUE_NOT_FOUND, Placeholder.parsed("id", info.input())));
            }

            return id;
        }).replaceSuggestions(ArgumentSuggestions.strings(info -> {
            return Teams.names().toArray(new String[0]);
        }));
    }
}
