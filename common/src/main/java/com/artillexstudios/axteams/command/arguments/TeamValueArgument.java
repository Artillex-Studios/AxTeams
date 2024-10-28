package com.artillexstudios.axteams.command.arguments;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.exception.RegistrationFailedException;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.config.Language;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public final class TeamValueArgument {

    public static Argument<Object> teamValue(String valueName) {
        return new CustomArgument<>(new StringArgument(valueName), info -> {
            try {
                return TeamValues.get(info.input());
            } catch (RegistrationFailedException e) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX + Language.TEAM_VALUE_NOT_FOUND, Placeholder.parsed("id", info.input())));
            }
        }).replaceSuggestions(ArgumentSuggestions.strings(info -> {
            return TeamValues.keys().toArray(new String[0]);
        }));
    }
}
