package com.artillexstudios.axteams.teams;

import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.LoadContext;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.database.DataHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class Teams {
    private static final TeamID EMPTY_TEAMID = new TeamID(-1);
    private static final ConcurrentHashMap<TeamID, Team> loadedTeams = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Team> unsaved = new ConcurrentLinkedQueue<>();
    private static final Map<String, TeamID> teamNames = new ConcurrentHashMap<>();
    private static final Cache<TeamID, Team> tempTeams = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    public static Team getTeamIfLoadedImmediately(TeamID teamID) {
        Team team = loadedTeams.get(teamID);
        if (team != null) {
            if (Config.debug) {
                LogUtils.debug("Team with id {} is loaded already!", teamID.id());
            }
            return team;
        }

        team = tempTeams.getIfPresent(teamID);
        if (team != null) {
            if (Config.debug) {
                LogUtils.debug("Team with id {} is temp loaded!", teamID.id());
            }
            return team;
        }

        Optional<Team> foundTeam = unsaved.stream().filter(t -> t.id().equals(teamID)).findAny();
        if (foundTeam.isPresent()) {
            if (Config.debug) {
                LogUtils.debug("Team with id {} is unsaved!", teamID.id());
            }
            team = foundTeam.get();
            tempTeams.put(teamID, team);
        }

        return team;
    }

    public static List<Team> loaded() {
        List<Team> teams = new ArrayList<>(loadedTeams.values());
        teams.addAll(tempTeams.asMap().values());
        return teams;
    }

    public static CompletableFuture<Team> loadTeam(TeamID teamID) {
        if (Config.debug) {
            LogUtils.debug("Team load called!");
        }
        Team team = tempTeams.getIfPresent(teamID);
        if (team != null) {
            if (Config.debug) {
                LogUtils.debug("Found already loaded team!");
            }
            tempTeams.invalidate(teamID);
            loadedTeams.put(teamID, team);
            return CompletableFuture.completedFuture(team);
        }

        if (Config.debug) {
            LogUtils.debug("Getting team!", new Throwable());
        }
        return getTeam(teamID, LoadContext.FULL);
    }

    public static CompletableFuture<Team> getTeam(TeamID teamID, LoadContext context) {
        Team team = getTeamIfLoadedImmediately(teamID);
        if (team != null) {
            if (Config.debug) {
                LogUtils.debug("Found team from getTeamIfLoadedImmediately for teamId {}.", teamID.id());
            }
            return CompletableFuture.completedFuture(team);
        }

        if (Config.debug) {
            LogUtils.debug("Loading unloaded team with id {}", teamID.id());
        }
        return DataHandler.loadTeam(teamID, context).toCompletableFuture();
    }

    public static void loadWithContext(Team team, LoadContext context) {
        switch (context) {
            case FULL -> loadedTeams.put(team.id(), team);
            case TEMPORARY -> tempTeams.put(team.id(), team);
        }
    }

    public static void disconnect(TeamID teamID) {
        Team team = loadedTeams.remove(teamID);

        if (team != null) {
            tempTeams.put(teamID, team);
        }
    }

    public static NameValidation validate(String name) {
        int length = name.length();
        if (length > Config.TeamName.maxLength) {
            return NameValidation.TOO_LONG;
        } else if (length < Config.TeamName.minLength) {
            return NameValidation.TOO_SHORT;
        }

        for (Pattern pattern : Config.TeamName.whitelist) {
            if (!pattern.asMatchPredicate().test(name)) {
                return NameValidation.NOT_WHITELISTED;
            }
        }

        for (Pattern pattern : Config.TeamName.blacklist) {
            if (pattern.asPredicate().test(name)) {
                return NameValidation.BLACKLISTED;
            }
        }

        return NameValidation.VALID;
    }

    public static void markUnsaved(Team team) {
        if (unsaved.contains(team)) {
            return;
        }

        unsaved.add(team);
    }

    public static ObjectArrayList<Team> unsaved() {
        ObjectArrayList<Team> teams = new ObjectArrayList<>(unsaved);
        unsaved.clear();
        return teams;
    }

    public static void changeName(String from, String to) {
        TeamID id = teamNames.remove(from);
        if (to != null && id != null) {
            teamNames.put(to, id);
        }
    }

    public static CompletableFuture<Void> disband(Team team) {
        unsaved.remove(team);
        return DataHandler.disband(team).thenRunAsync(() -> {
            Teams.changeName(team.name(), null);
            team.leader().team(null);
            for (User member : team.members()) {
                member.team(null);
            }
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while disbanding team!", throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public static CompletableFuture<com.artillexstudios.axteams.teams.Team> create(User user, String name) {
        teamNames.put(name, EMPTY_TEAMID);
        return DataHandler.createTeam(user, name).thenApply(team -> {
            if (team == null) {
                return null;
            }

            teamNames.put(name, team.id());
            return team;
        });
    }

    public static boolean doesNameExist(String name) {
        return teamNames.containsKey(name);
    }

    public static void loadNames(Map<String, TeamID> teams) {
        teamNames.putAll(teams);
    }

    public static Set<String> names() {
        return teamNames.keySet();
    }

    public static TeamID byName(String name) {
        return teamNames.get(name);
    }

    public static String byId(TeamID id) {
        for (Map.Entry<String, TeamID> entry : teamNames.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }

        return "";
    }
}
