package com.artillexstudios.axteams.command;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableBoolean;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableComponent;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableLocation;
import com.artillexstudios.axteams.api.teams.values.identifiables.Warp;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.command.arguments.TeamArgument;
import com.artillexstudios.axteams.command.arguments.TeamMemberArgument;
import com.artillexstudios.axteams.command.arguments.TeamValueArgument;
import com.artillexstudios.axteams.command.arguments.WarpArgument;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.guis.implementation.MainGui;
import com.artillexstudios.axteams.teams.NameValidation;
import com.artillexstudios.axteams.teams.Teams;
import com.artillexstudios.axteams.users.Users;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum AxTeamsCommand {
    INSTANCE;

    public void register() {
        new CommandTree("axteams")
                .withAliases("axteam", "teams", "team")
                .then(new LiteralArgument("create")
                        .then(new StringArgument("name")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    if (user.team() != null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.ALREADY_IN_TEAM);
                                        return;
                                    }

                                    String name = args.getByClass("name", String.class);
                                    if (name == null) {
                                        return;
                                    }

                                    if (Teams.doesNameExist(name)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.ALREADY_EXISTS);
                                        return;
                                    }

                                    NameValidation validation = Teams.validate(name);
                                    switch (validation) {
                                        case TOO_SHORT ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.TOO_SHORT);
                                        case TOO_LONG ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.TOO_LONG);
                                        case BLACKLISTED ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.BLACKLISTED);
                                        case NOT_WHITELISTED ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_WHITELISTED);
                                        case VALID -> Teams.create(user, name).thenAccept(team -> {
                                            team.add(TeamValues.TEAM_DISPLAY_NAME, new IdentifiableComponent(Component.text(name)));
                                            MessageUtils.sendMessage(sender, Language.PREFIX, Language.CREATED);
                                        });
                                    }
                                })
                        )
                )
                .then(new LiteralArgument("gui")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }

                            LogUtils.info("open");
                            new MainGui(user).open();
                        })
                )
                .then(new LiteralArgument("disband")
                        .executesPlayer((sender, args) -> {
                            // TODO: Confirm
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }

                            if (team.leader() != user) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_LEADER);
                                return;
                            }

                            team.disband().thenRun(() -> {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.DISBANDED);
                            });
                        })
                )
                .then(new LiteralArgument("list")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }

                            new MainGui(user).open();
                        })
                )
                .then(new LiteralArgument("chat")
                        .then(new GreedyStringArgument("message")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }

                                    for (User member : team.members(true)) {

                                    }
                                })
                        )
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }


                        })
                )
                .then(new LiteralArgument("leave")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }

                            if (user.equals(team.leader())) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, "You are the leader!");
                                return;
                            }

                            team.remove(user);
                            // TODO: Leave message
                            MessageUtils.sendMessage(sender, Language.PREFIX, "left");
                        })
                )
                .then(new LiteralArgument("allychat")
                        .then(new GreedyStringArgument("message")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }


                                })
                        )
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }


                        })
                )
                .then(new LiteralArgument("home")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }

                            Location home = team.first(TeamValues.HOME);
                            if (home == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, "no home set");
                                return;
                            }

                            PaperUtils.teleportAsync(sender, home).thenAccept((success) -> {
                                // TODO: teleport message
                            });
                        })
                )
                .then(new LiteralArgument("sethome")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }

                            if (!team.hasPermission(user, Permissions.HOME_CREATE)) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                return;
                            }

                            team.add(TeamValues.HOME, new IdentifiableLocation(sender.getLocation()));
                            // TODO: feedback
                        })
                )
                .then(new LiteralArgument("pvp")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                return;
                            }

                            if (!team.hasPermission(user, Permissions.PVP)) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                return;
                            }

                            Boolean pvp = team.first(TeamValues.PVP);
                            if (pvp == null) {
                                pvp = false;
                            }

                            team.add(TeamValues.PVP, new IdentifiableBoolean(!pvp));
                            LogUtils.info("State: {}", !pvp);
                            // TODO: Send message
                        })
                )
                .then(new LiteralArgument("transfer")
                        .then(TeamMemberArgument.teamMember("other")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }

                                    if (team.leader() != user) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_LEADER);
                                        return;
                                    }

                                    User other = args.getByClass("other", User.class);
                                    if (other == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "No member found");
                                        return;
                                    }

                                    team.leader(other);
                                    // TODO: feedback
                                })
                        )
                )
                .then(new LiteralArgument("warp")
                        .then(WarpArgument.senderWarp("warp")
                                .then(new GreedyStringArgument("password")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                                return;
                                            }

                                            Team team = user.team();
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                                return;
                                            }

                                            Warp warp = args.getByClass("warp", Warp.class);
                                            if (warp == null) {
                                                return;
                                            }

                                            String password = args.getByClass("password", String.class);
                                            if (!warp.password().isBlank() && !warp.password().equals(password)) {
                                                // TODO: Send incorrect password message
                                                return;
                                            }

                                            PaperUtils.teleportAsync(sender, warp.location()).thenAccept(accepted -> {
                                                // TODO: Send teleported message
                                            });
                                        })
                                )
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }

                                    Warp warp = args.getByClass("warp", Warp.class);
                                    if (warp == null) {
                                        return;
                                    }

                                    if (warp.password() != null) {
                                        // TODO: Send needs password message
                                        return;
                                    }

                                    PaperUtils.teleportAsync(sender, warp.location()).thenAccept(accepted -> {
                                        // TODO: Send teleported message
                                    });
                                })
                        )
                )
                .then(new LiteralArgument("setwarp")
                        .then(new StringArgument("name")
                                .then(new StringArgument("password")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                                return;
                                            }

                                            Team team = user.team();
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                                return;
                                            }

                                            if (!team.hasPermission(user, Permissions.WARP_CREATE)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                                return;
                                            }

                                            Integer warpLimit = team.first(TeamValues.WARP_LIMIT);
                                            List<Warp> warps = team.values(TeamValues.WARPS);
                                            warpLimit = warpLimit == null ? 10 : warpLimit; // TODO: Configurable limit
                                            if (warps.size() + 1 > warpLimit) {
                                                // TODO: Limit reached message
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                                return;
                                            }

                                            String name = args.getByClass("name", String.class);
                                            if (name == null) {
                                                return;
                                            }

                                            String password = args.getByClass("password", String.class);
                                            if (password == null) {
                                                return;
                                            }

                                            Warp warp = new Warp(name, sender.getLocation(), password);
                                            team.add(TeamValues.WARPS, warp);
                                            // TODO: Created message
                                        })
                                )
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }

                                    if (!team.hasPermission(user, Permissions.WARP_CREATE)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                        return;
                                    }

                                    Integer warpLimit = team.first(TeamValues.WARP_LIMIT);
                                    List<Warp> warps = team.values(TeamValues.WARPS);
                                    warpLimit = warpLimit == null ? 10 : warpLimit; // TODO: Configurable limit
                                    if (warps.size() + 1 > warpLimit) {
                                        // TODO: Limit reached message
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "limit reached");
                                        return;
                                    }

                                    String name = args.getByClass("name", String.class);
                                    if (name == null) {
                                        return;
                                    }

                                    // TODO: Check if warp with that name exists
                                    Warp warp = new Warp(name, sender.getLocation(), "");
                                    team.add(TeamValues.WARPS, warp);
                                    MessageUtils.sendMessage(sender, Language.PREFIX, "created warp");
                                })
                        )
                )
                .then(new LiteralArgument("invite")
                        .then(new PlayerArgument("player")
                                .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                                    Team team = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()).team() : null;
                                    if (team == null) {
                                        return List.of();
                                    }

                                    List<String> memberNames = team.members(true).stream().map(user -> user.player().getName()).toList();
                                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> !memberNames.contains(name)).toList();
                                })).executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    User invited = AxTeamsAPI.instance().getUserIfLoadedImmediately(args.getByClass("player", Player.class));
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }

                                    if (!team.hasPermission(user, Permissions.INVITE)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                        return;
                                    }

                                    if (team.members().contains(invited)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "already member");
                                        return;
                                    }

                                    // TODO: Check if they are banned
                                    if (team.invited(invited)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "already invited");
                                        return;
                                    }

                                    // TODO: messages
                                    team.invite(invited);
                                })
                        )
                )
                .then(new LiteralArgument("join")
                        .then(TeamArgument.team("team")
                                .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                                    User user = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()) : null;
                                    Team team = user.team();
                                    if (team != null) {
                                        return List.of();
                                    }

                                    List<Team> teams = Teams.loaded();

                                    return teams.stream().filter(s -> s.invited(user)).map(Team::name).toList();
                                })).executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team != null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "in team");
                                        return;
                                    }

                                    Team joiningTeam = Teams.getTeamIfLoadedImmediately(args.getByClass("team", TeamID.class));

                                    // TODO: Check if they are banned
                                    if (!joiningTeam.invited(user)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "Not invited");
                                        return;
                                    }

                                    // TODO: messages
                                    // TODO: team size limit check
                                    joiningTeam.removeInvite(user);
                                    joiningTeam.add(user);
                                    MessageUtils.sendMessage(sender, Language.PREFIX, "joined!");
                                })
                        )
                )
                .then(new LiteralArgument("ally")
                        .then(new LiteralArgument("accept")
                                .then(TeamArgument.team("team")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                                return;
                                            }

                                            Team userTeam = user.team();
                                            if (userTeam == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                                return;
                                            }

                                            TeamID teamID = args.getByClass("team", TeamID.class);
                                            if (teamID == null) {
                                                return;
                                            }

                                            Team team = Teams.getTeamIfLoadedImmediately(teamID);
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Team is not loaded");
                                                return;
                                            }

                                            if (userTeam.isAlly(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Already allies!");
                                                return;
                                            }

                                            if (!userTeam.hasAllyRequest(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "No request");
                                                return;
                                            }


                                            userTeam.removeAllyRequest(teamID);
                                            team.removeAllyRequest(userTeam.id());
                                            team.addAlly(userTeam.id());
                                            userTeam.addAlly(teamID);
                                            // TODO: Message
                                        })
                                )
                        )
                        .then(new LiteralArgument("invite")
                                .then(TeamArgument.team("team")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                                return;
                                            }

                                            Team userTeam = user.team();
                                            if (userTeam == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                                return;
                                            }

                                            if (!userTeam.hasPermission(user, Permissions.ALLY_SEND)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                                return;
                                            }

                                            TeamID teamID = args.getByClass("team", TeamID.class);
                                            if (teamID == null) {
                                                return;
                                            }

                                            Team team = Teams.getTeamIfLoadedImmediately(teamID);
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Team is not loaded");
                                                return;
                                            }

                                            if (userTeam.isAlly(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Already allies!");
                                                return;
                                            }

                                            if (team.hasAllyRequest(userTeam.id())) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Already sent request!");
                                                return;
                                            }

                                            if (userTeam.hasAllyRequest(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "You have an incoming request! Use ally accept to accept!");
                                                return;
                                            }

                                            team.addAllyRequest(userTeam.id());
                                            userTeam.addAllyRequest(teamID);
                                            // TODO: Messages
                                        })
                                )
                        )
                        .then(new LiteralArgument("remove")
                                .then(TeamArgument.team("team")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                                return;
                                            }

                                            Team userTeam = user.team();
                                            if (userTeam == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                                return;
                                            }

                                            TeamID teamID = args.getByClass("team", TeamID.class);
                                            if (teamID == null) {
                                                return;
                                            }

                                            Team team = Teams.getTeamIfLoadedImmediately(teamID);
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Team is not loaded");
                                                return;
                                            }

                                            if (!userTeam.isAlly(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Not allies!");
                                                return;
                                            }

                                            userTeam.removeAlly(teamID);
                                            team.removeAlly(userTeam.id());
                                            // TODO: removed message
                                        })
                                )
                        )
                )
                .then(new LiteralArgument("kick")
                        .then(TeamMemberArgument.teamMember("member")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }

                                    User other = args.getByClass("member", User.class);
                                    if (other == null) {
                                        return;
                                    }

                                    if (!team.hasPermission(user, other, Permissions.KICK)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                        return;
                                    }

                                    team.remove(other);
                                    MessageUtils.sendMessage(sender, Language.PREFIX, "removed player");
                                })
                        )
                )
                .then(new LiteralArgument("rename")
                        .then(new StringArgument("name")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.USER_NOT_LOADED);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_IN_TEAM);
                                        return;
                                    }

                                    if (!user.equals(team.leader())) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_LEADER);
                                        return;
                                    }

                                    String name = args.getByClass("name", String.class);
                                    if (name == null) {
                                        return;
                                    }

                                    if (Teams.doesNameExist(name)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.ALREADY_EXISTS);
                                        return;
                                    }

                                    NameValidation validation = Teams.validate(name);
                                    switch (validation) {
                                        case TOO_SHORT ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.TOO_SHORT);
                                        case TOO_LONG ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.TOO_LONG);
                                        case BLACKLISTED ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.BLACKLISTED);
                                        case NOT_WHITELISTED ->
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NOT_WHITELISTED);
                                        case VALID -> {
                                            Teams.changeName(team.name(), name);
                                            team.name(name);
                                        }
                                    }
                                })
                        )
                )
                .then(new LiteralArgument("admin")
                        .withPermission("axteams.command.admin")
                        .then(new LiteralArgument("get")
                                .then(TeamArgument.team("team")
                                        .then(TeamValueArgument.teamValue("teamvalue")
                                                .executes((sender, args) -> {
                                                    TeamID teamID = args.getByClass("team", TeamID.class);
                                                    TeamValue value = args.getByClass("teamvalue", TeamValue.class);

                                                    AxTeamsAPI.instance().team(teamID).thenAccept(team -> {
                                                        sender.sendMessage("Value: " + team.values(value));
                                                    });
                                                })
                                        )
                                )
                        )
                        .then(new LiteralArgument("info")
                                .then(new OfflinePlayerArgument("player")
                                        .executes((sender, args) -> {
                                            OfflinePlayer player = args.getByClass("player", OfflinePlayer.class);
                                            AxTeamsAPI.instance().user(player.getUniqueId()).thenAccept(user -> {
                                                Team team = user.team();
                                                sender.sendMessage("User id: %s team id: %s".formatted(user.id(), team == null ? "no team" : team.name()));
                                            });
                                        })
                                )
                        )
                        .then(new LiteralArgument("benchmark")
                                .then(new IntegerArgument("amount")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                return;
                                            }

                                            long start = System.nanoTime();
                                            int amount = args.getByClass("amount", Integer.class);
                                            for (int i = 0; i < amount; i++) {
                                                for (TeamValue<?, ?> value : TeamValues.values()) {
                                                    user.team().values(value);
                                                }
                                            }

                                            long took = System.nanoTime() - start;
                                            sender.sendMessage("Debug took: %s ns (%s ms).".formatted(took, took / 1_000_000));
                                        })
                                )
                        )
                        .then(new LiteralArgument("version")
                                .withPermission("axteams.command.admin.version")
                                .executes((sender, args) -> {
                                    MessageUtils.sendMessage(sender, Language.PREFIX, "<green>You are running <white>AxTeams</white> version <white><version></white> on <white><implementation></white> version <white><implementation-version></white> (Implementing API version <white><api-version></white>)",
                                            Placeholder.unparsed("version", AxTeamsPlugin.instance().getDescription().getVersion()),
                                            Placeholder.unparsed("implementation", Bukkit.getName()),
                                            Placeholder.unparsed("implementation-version", Bukkit.getVersion()),
                                            Placeholder.unparsed("api-version", Bukkit.getBukkitVersion())
                                    );
                                })
                        )
                        .then(new LiteralArgument("reload")
                                .withPermission("axteams.command.admin.reload")
                                .executes((sender, args) -> {
                                    long start = System.nanoTime();
                                    List<String> failed = new ArrayList<>();

                                    if (!Config.reload()) {
                                        failed.add("config.yml");
                                    }

                                    if (!Language.reload()) {
                                        failed.add("language/" + Language.lastLanguage + ".yml");
                                    }

                                    if (failed.isEmpty()) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.RELOAD_SUCCESS, Placeholder.parsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)));
                                    } else {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.RELOAD_FAIL, Placeholder.parsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)), Placeholder.parsed("files", String.join(", ", failed)));
                                    }
                                })
                        )
                )
                .register();
    }
}
