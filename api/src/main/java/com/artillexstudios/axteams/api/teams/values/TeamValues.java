package com.artillexstudios.axteams.api.teams.values;

import com.artillexstudios.axteams.api.exception.RegistrationFailedException;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableBigDecimal;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableBoolean;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableComponent;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableInteger;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableLocation;
import com.artillexstudios.axteams.api.teams.values.identifiables.Warp;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamAlliesValue;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamBankValue;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamDisplayNameValue;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamGroupsValue;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamHomeValue;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamPvPValue;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamWarpLimitValue;
import com.artillexstudios.axteams.api.teams.values.implementation.TeamWarpsValue;
import com.artillexstudios.axteams.api.utils.Registry;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public final class TeamValues {
    private static final Registry<String, TeamValue<?, ?>> registry = new Registry<>();
    private static final Logger logger = LoggerFactory.getLogger(TeamValues.class);
    public static final TeamValue<Component, IdentifiableComponent> TEAM_DISPLAY_NAME = register(new TeamDisplayNameValue());
    public static final TeamValue<Boolean, IdentifiableBoolean> PVP = register(new TeamPvPValue());
    public static final TeamValue<Warp, Warp> WARPS = register(new TeamWarpsValue());
    public static final TeamValue<Integer, IdentifiableInteger> WARP_LIMIT = register(new TeamWarpLimitValue());
    public static final TeamValue<Location, IdentifiableLocation> HOME = register(new TeamHomeValue());
    public static final TeamValue<Integer, IdentifiableInteger> ALLIES = register(new TeamAlliesValue());
    public static final TeamValue<Group, Group> GROUPS = register(new TeamGroupsValue());
    public static final TeamValue<BigDecimal, IdentifiableBigDecimal> BANK = register(new TeamBankValue());

    public static <Y, T extends Identifiable<Y>> TeamValue<Y, T> register(TeamValue<Y, T> teamValue) {
        Preconditions.checkNotNull(teamValue, "Tried to register null teamValue!");
        try {
            registry.register(teamValue.id().toLowerCase(Locale.ENGLISH), teamValue);
            return teamValue;
        } catch (RegistrationFailedException exception) {
            logger.error("Failed to register TeamValue with id {}! Cause: {}", exception.key().toString(), exception.cause());
            return null;
        }
    }

    public static <Y, T extends Identifiable<Y>> void deregister(TeamValue<Y, T> teamValue) {
        Preconditions.checkNotNull(teamValue, "Tried to deregister null teamValue!");
        try {
            registry.deregister(teamValue.id().toLowerCase(Locale.ENGLISH));
        } catch (RegistrationFailedException exception) {
            logger.error("Failed to deregister TeamValue with id {}! Cause: {}", exception.key().toString(), exception.cause());
        }
    }

    public static <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> Z get(String teamValue) throws RegistrationFailedException {
        Preconditions.checkNotNull(teamValue, "Tried to get null teamValue!");
        return (Z) registry.get(teamValue.toLowerCase(Locale.ENGLISH));
    }

    public static Set<String> keys() {
        return registry.keys();
    }

    public static Collection<TeamValue<?, ?>> values() {
        return registry.values();
    }
}
