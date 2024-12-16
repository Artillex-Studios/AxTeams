package com.artillexstudios.axteams.command;

import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.events.PreTeamInviteEvent;
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
import com.artillexstudios.axteams.api.users.settings.UserSettings;
import com.artillexstudios.axteams.command.arguments.TeamArgument;
import com.artillexstudios.axteams.command.arguments.TeamMemberArgument;
import com.artillexstudios.axteams.command.arguments.TeamValueArgument;
import com.artillexstudios.axteams.command.arguments.WarpArgument;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.config.Groups;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.config.Levels;
import com.artillexstudios.axteams.guis.implementation.MainGui;
import com.artillexstudios.axteams.guis.implementation.UsersGui;
import com.artillexstudios.axteams.teams.NameValidation;
import com.artillexstudios.axteams.teams.Teams;
import com.artillexstudios.axteams.users.Users;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum AxTeamsCommand {
    INSTANCE;
    private final Cache<UUID, Boolean> disbandCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(1000)
            .build();

    public void register() {
        new CommandTree("axteams")
                .withAliases("axteam", "teams", "team")
                .then(new LiteralArgument("create")
                        .then(new TextArgument("name")
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
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Failed to create team! No owner group is set up! Please inform server administrators!");
                                                return;
                                            }

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

                            new MainGui(user).open();
                        })
                )
                .then(new LiteralArgument("disband")
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

                            if (this.disbandCache.getIfPresent(sender.getUniqueId()) != null) {
                                this.disbandCache.invalidate(sender.getUniqueId());
                                team.disband().thenRun(() -> {
                                    MessageUtils.sendMessage(sender, Language.PREFIX, Language.DISBANDED);
                                });
                            } else {
                                this.disbandCache.put(sender.getUniqueId(), true);
                                // TODO: Message
                            }
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

                            new UsersGui(user).open();
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

                                    Map<String, String> placeholders = Placeholders.asMap(Context.builder(ParseContext.INTERNAL).add(String.class, args.getByClass("message", String.class)).add(User.class, user));
                                    if (Config.DEBUG) {
                                        LogUtils.debug("Placeholders in message send: {}", placeholders);
                                    }

                                    team.message(StringUtils.format(Language.TEAM_CHAT_FORMAT, placeholders));
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

                            boolean allyChat = UserSettings.ALLY_CHAT_TOGGLED.read(user);
                            boolean teamChat = UserSettings.TEAM_CHAT_TOGGLED.read(user);
                            if (allyChat) {
                                UserSettings.ALLY_CHAT_TOGGLED.write(user, false);
                                UserSettings.TEAM_CHAT_TOGGLED.write(user, true);
                            } else {
                                UserSettings.TEAM_CHAT_TOGGLED.write(user, !teamChat);
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

                                    Component message = StringUtils.format(Language.ALLY_CHAT_FORMAT, Placeholders.asMap(Context.builder(ParseContext.INTERNAL).add(String.class, args.getByClass("message", String.class)).add(User.class, user)));
                                    team.message(message);

                                    for (Integer value : team.values(TeamValues.ALLIES)) {
                                        Team ally = AxTeamsAPI.instance().getTeamIfLoadedImmediately(new TeamID(value));
                                        if (ally != null) {
                                            ally.message(message);
                                        }
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

                            boolean allyChat = UserSettings.ALLY_CHAT_TOGGLED.read(user);
                            boolean teamChat = UserSettings.TEAM_CHAT_TOGGLED.read(user);
                            if (teamChat) {
                                UserSettings.TEAM_CHAT_TOGGLED.write(user, false);
                                UserSettings.ALLY_CHAT_TOGGLED.write(user, true);
                            } else {
                                UserSettings.ALLY_CHAT_TOGGLED.write(user, !allyChat);
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

                            if (!user.hasPermission(Permissions.HOME_CREATE)) {
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

                            if (!user.hasPermission(Permissions.PVP)) {
                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                return;
                            }

                            Boolean pvp = team.first(TeamValues.PVP);
                            if (pvp == null) {
                                pvp = false;
                            }

                            team.add(TeamValues.PVP, new IdentifiableBoolean(!pvp));
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


                                    other.group(team.leader().group());
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
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "Incorrect password");
                                                return;
                                            }

                                            PaperUtils.teleportAsync(sender, warp.location()).thenAccept(accepted -> {
                                                // TODO: Send teleported message
                                                MessageUtils.sendMessage(sender, Language.PREFIX, "teleported to warp");
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

                                    if (warp.password() != null && !warp.password().isBlank()) {
                                        // TODO: Send needs password message
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "needs password");
                                        return;
                                    }

                                    PaperUtils.teleportAsync(sender, warp.location()).thenAccept(accepted -> {
                                        // TODO: Send teleported message
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "teleported to warp");
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

                                            if (!user.hasPermission(Permissions.WARP_CREATE)) {
                                                MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                                return;
                                            }

                                            Integer warpLimit = team.first(TeamValues.WARP_LIMIT);
                                            List<Warp> warps = team.values(TeamValues.WARPS);
                                            warpLimit = warpLimit == null ? Config.DEFAULT_WARP_LIMIT : warpLimit;
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

                                    if (!user.hasPermission(Permissions.WARP_CREATE)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                        return;
                                    }

                                    Integer warpLimit = team.first(TeamValues.WARP_LIMIT);
                                    List<Warp> warps = team.values(TeamValues.WARPS);
                                    warpLimit = warpLimit == null ? Config.DEFAULT_WARP_LIMIT : warpLimit;
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

                                    if (!user.hasPermission(Permissions.INVITE)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, Language.NO_PERMISSION);
                                        return;
                                    }

                                    if (team.members(true).contains(invited)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "already member");
                                        return;
                                    }

                                    if (team.invited(invited)) {
                                        MessageUtils.sendMessage(sender, Language.PREFIX, "already invited");
                                        return;
                                    }

                                    if (!new PreTeamInviteEvent(team, user, invited).call()) {
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
                                .then(TeamArgument.invited("team")
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

                                            if (!user.hasPermission(Permissions.ALLY_SEND)) {
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
                                .then(TeamArgument.ally("team")
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

                                    if (!user.hasPermission(Permissions.KICK, other)) {
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
                .then(new LiteralArgument("bank")
                        .then(new LiteralArgument("balance")

                        )
                        .then(new LiteralArgument("take")

                        )
                        .then(new LiteralArgument("insert")

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

                                    if (!Groups.reload()) {
                                        failed.add("groups.yml");
                                    }

                                    if (!Levels.reload()) {
                                        failed.add("levels.yml");
                                    }

                                    if (!com.artillexstudios.axteams.config.Permissions.reload()) {
                                        failed.add("permissions.yml");
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
