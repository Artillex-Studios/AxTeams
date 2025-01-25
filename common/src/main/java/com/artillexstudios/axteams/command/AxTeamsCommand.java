package com.artillexstudios.axteams.command;

import com.artillexstudios.axapi.libs.caffeine.caffeine.cache.Cache;
import com.artillexstudios.axapi.libs.caffeine.caffeine.cache.Caffeine;
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
import com.artillexstudios.axteams.api.events.TeamInviteEvent;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableBigDecimal;
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
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
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
                        .then(new GreedyStringArgument("name")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team userTeam = user.team();
                                    if (userTeam != null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.alreadyInTeam, Placeholder.unparsed("team", userTeam.name()));
                                        return;
                                    }

                                    String name = args.getByClass("name", String.class);
                                    if (name == null) {
                                        return;
                                    }
                                    name = name.split(" ")[0];
                                    String finalName = name;

                                    if (Teams.doesNameExist(name)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.alreadyExists, Placeholder.unparsed("name", name));
                                        return;
                                    }

                                    NameValidation validation = Teams.validate(name);
                                    switch (validation) {
                                        case TOO_SHORT ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.tooShort, Placeholder.unparsed("name", name));
                                        case TOO_LONG ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.tooLong, Placeholder.unparsed("name", name));
                                        case BLACKLISTED ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.blacklisted, Placeholder.unparsed("name", name));
                                        case NOT_WHITELISTED ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.notWhitelisted, Placeholder.unparsed("name", name));
                                        case VALID -> Teams.create(user, name).thenAccept(team -> {
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.failedToCreate);
                                                return;
                                            }

                                            team.postLoad();
                                            team.add(TeamValues.TEAM_DISPLAY_NAME, new IdentifiableComponent(Component.text(finalName)));
                                            MessageUtils.sendMessage(sender, Language.prefix, Language.success.created, Placeholder.unparsed("name", finalName));
                                        });
                                    }
                                })
                        )
                )
                .then(new LiteralArgument("gui")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                return;
                            }

                            new MainGui(user).open();
                        })
                )
                .then(new LiteralArgument("disband")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                return;
                            }

                            if (team.leader() != user) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notLeader);
                                return;
                            }

                            if (this.disbandCache.getIfPresent(sender.getUniqueId()) != null) {
                                this.disbandCache.invalidate(sender.getUniqueId());
                                team.disband().thenRun(() -> {
                                    MessageUtils.sendMessage(sender, Language.prefix, Language.success.disbanded);
                                });
                            } else {
                                this.disbandCache.put(sender.getUniqueId(), true);
                                MessageUtils.sendMessage(sender, Language.prefix, Language.success.disbandConfirmation);
                            }
                        })
                )
                .then(new LiteralArgument("list")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
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
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    Map<String, String> placeholders = Placeholders.asMap(Context.builder(ParseContext.INTERNAL).add(String.class, args.getByClass("message", String.class)).add(User.class, user));
                                    if (Config.debug) {
                                        LogUtils.debug("Placeholders in message send: {}", placeholders);
                                    }

                                    team.message(StringUtils.format(Language.chatFormat.team, placeholders));
                                })
                        )
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
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

                            MessageUtils.sendMessage(sender, Language.prefix, teamChat ? Language.success.chatToggle.teamChat.disable : Language.success.chatToggle.teamChat.enable);
                        })
                )
                .then(new LiteralArgument("leave")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                return;
                            }

                            if (user.equals(team.leader())) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.leader);
                                return;
                            }

                            team.remove(user);
                            MessageUtils.sendMessage(sender, Language.prefix, Language.success.leave);
                            team.message(StringUtils.format(Language.prefix + Language.success.leaveAnnouncement, Placeholder.unparsed("player", sender.getName())));
                        })
                )
                .then(new LiteralArgument("allychat")
                        .then(new GreedyStringArgument("message")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    Component message = StringUtils.format(Language.chatFormat.ally, Placeholders.asMap(Context.builder(ParseContext.INTERNAL).add(String.class, args.getByClass("message", String.class)).add(User.class, user)));
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
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
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

                            MessageUtils.sendMessage(sender, Language.prefix, allyChat ? Language.success.chatToggle.allyChat.disable : Language.success.chatToggle.allyChat.enable);
                        })
                )
                .then(new LiteralArgument("home")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                return;
                            }

                            Location home = team.first(TeamValues.HOME);
                            if (home == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noHomeSet);
                                return;
                            }

                            PaperUtils.teleportAsync(sender, home).thenAccept((success) -> {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.success.teleportHome);
                            });
                        })
                )
                .then(new LiteralArgument("sethome")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                return;
                            }

                            if (!user.hasPermission(Permissions.HOME_CREATE)) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                return;
                            }

                            team.add(TeamValues.HOME, new IdentifiableLocation(sender.getLocation()));
                            MessageUtils.sendMessage(sender, Language.prefix, Language.success.setHome);
                        })
                )
                .then(new LiteralArgument("pvp")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                return;
                            }

                            if (!user.hasPermission(Permissions.PVP)) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                return;
                            }

                            Boolean pvp = team.first(TeamValues.PVP);
                            if (pvp == null) {
                                pvp = false;
                            }

                            team.add(TeamValues.PVP, new IdentifiableBoolean(!pvp));
                            MessageUtils.sendMessage(sender, Language.prefix, pvp ? Language.success.pvPToggle.disable : Language.success.pvPToggle.enable);
                        })
                )
                .then(new LiteralArgument("transfer")
                        .then(TeamMemberArgument.teamMember("other")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    if (team.leader() != user) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notLeader);
                                        return;
                                    }

                                    User other = args.getByClass("other", User.class);
                                    if (other == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.unknownMember);
                                        return;
                                    }

                                    other.group(team.leader().group());
                                    team.leader(other);
                                    MessageUtils.sendMessage(sender, Language.prefix, Language.success.transfer, Placeholder.unparsed("player", other.name()));
                                    team.message(StringUtils.format(Language.prefix + Language.success.transferAnnouncement, Placeholder.unparsed("from", user.name()), Placeholder.unparsed("to", other.name())));
                                })
                        )
                )
                .then(new LiteralArgument("warp")
                        .then(WarpArgument.senderWarp("warp")
                                .then(new GreedyStringArgument("password")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                                return;
                                            }

                                            Team team = user.team();
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                                return;
                                            }

                                            Warp warp = args.getByClass("warp", Warp.class);
                                            if (warp == null) {
                                                return;
                                            }

                                            String password = args.getByClass("password", String.class);
                                            password = password.split(" ")[0];
                                            if (!warp.password().isBlank() && !warp.password().equals(password)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.warp.incorrectPassword, Placeholder.unparsed("name", warp.name()));
                                                return;
                                            }

                                            PaperUtils.teleportAsync(sender, warp.location()).thenAccept(accepted -> {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.warp.teleported, Placeholder.unparsed("name", warp.name()));
                                            });
                                        })
                                )
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    Warp warp = args.getByClass("warp", Warp.class);
                                    if (warp == null) {
                                        return;
                                    }

                                    if (warp.password() != null && !warp.password().isBlank()) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.warp.hasPassword, Placeholder.unparsed("name", warp.name()));
                                        return;
                                    }

                                    PaperUtils.teleportAsync(sender, warp.location()).thenAccept(accepted -> {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.warp.teleported, Placeholder.unparsed("name", warp.name()));
                                    });
                                })
                        )
                )
                .then(new LiteralArgument("setwarp")
                        .then(new StringArgument("name")
                                .then(new GreedyStringArgument("password")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                                return;
                                            }

                                            Team team = user.team();
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                                return;
                                            }

                                            if (!user.hasPermission(Permissions.WARP_CREATE)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                                return;
                                            }

                                            Integer warpLimit = team.first(TeamValues.WARP_LIMIT);
                                            List<Warp> warps = team.values(TeamValues.WARPS);
                                            warpLimit = warpLimit == null ? Config.defaultWarpLimit : warpLimit;
                                            if (warps.size() + 1 > warpLimit) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.warp.limitReached, Placeholder.unparsed("warps", String.valueOf(warps.size())), Placeholder.unparsed("limit", String.valueOf(warpLimit)));
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
                                            password = password.split(" ")[0];

                                            for (Warp value : warps) {
                                                if (value.name().equalsIgnoreCase(name)) {
                                                    MessageUtils.sendMessage(sender, Language.prefix, Language.warp.alreadyExists, Placeholder.unparsed("name", name));
                                                    return;
                                                }
                                            }

                                            Warp warp = new Warp(name, sender.getLocation(), password);
                                            team.add(TeamValues.WARPS, warp);
                                            MessageUtils.sendMessage(sender, Language.prefix, Language.warp.created, Placeholder.unparsed("name", name));
                                        })
                                )
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    if (!user.hasPermission(Permissions.WARP_CREATE)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                        return;
                                    }

                                    Integer warpLimit = team.first(TeamValues.WARP_LIMIT);
                                    List<Warp> warps = team.values(TeamValues.WARPS);
                                    warpLimit = warpLimit == null ? Config.defaultWarpLimit : warpLimit;
                                    if (warps.size() + 1 > warpLimit) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.warp.limitReached, Placeholder.unparsed("warps", String.valueOf(warps.size())), Placeholder.unparsed("limit", String.valueOf(warpLimit)));
                                        return;
                                    }

                                    String name = args.getByClass("name", String.class);
                                    if (name == null) {
                                        return;
                                    }

                                    for (Warp value : warps) {
                                        if (value.name().equalsIgnoreCase(name)) {
                                            MessageUtils.sendMessage(sender, Language.prefix, Language.warp.alreadyExists, Placeholder.unparsed("name", name));
                                            return;
                                        }
                                    }

                                    Warp warp = new Warp(name, sender.getLocation(), "");
                                    team.add(TeamValues.WARPS, warp);
                                    MessageUtils.sendMessage(sender, Language.prefix, Language.warp.created, Placeholder.unparsed("name", name));
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

                                    List<String> memberNames = team.members(member -> member.onlinePlayer() != null, true)
                                            .stream()
                                            .map(User::name)
                                            .toList();

                                    return Bukkit.getOnlinePlayers()
                                            .stream()
                                            .map(Player::getName)
                                            .filter(name -> !memberNames.contains(name))
                                            .toList();
                                })).executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    if (!user.hasPermission(Permissions.INVITE)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                        return;
                                    }

                                    User invited = AxTeamsAPI.instance().getUserIfLoadedImmediately(args.getByClass("player", Player.class));
                                    if (invited == null) {
                                        return;
                                    }

                                    if (!new PreTeamInviteEvent(team, user, invited).call()) {
                                        return;
                                    }

                                    if (team.members(true).contains(invited)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.invite.alreadyMember);
                                        return;
                                    }

                                    if (team.invited(invited)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.invite.alreadyInvited);
                                        return;
                                    }

                                    int memberSize = team.members(true).size();
                                    Integer sizeLimit = team.first(TeamValues.TEAM_SIZE_LIMIT);
                                    sizeLimit = sizeLimit == null ? Config.defaultTeamSizeLimit : sizeLimit;
                                    if (memberSize + 1 > sizeLimit) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.invite.teamIsFull);
                                        return;
                                    }

                                    new TeamInviteEvent(team, user, invited).call();
                                    team.invite(invited);
                                    MessageUtils.sendMessage(sender, Language.prefix, Language.invite.inviteSent, Placeholder.unparsed("player", sender.getName()));
                                    team.message(StringUtils.format(Language.prefix + Language.invite.inviteSentAnnouncement, Placeholder.unparsed("other", invited.name()), Placeholder.unparsed("player", sender.getName())));
                                    MessageUtils.sendMessage(invited.onlinePlayer(), Language.prefix, Language.invite.inviteReceived, Placeholder.unparsed("player", sender.getName()), Placeholder.unparsed("team", team.name()));
                                })
                        )
                )
                .then(new LiteralArgument("join")
                        .then(TeamArgument.team("team")
                                .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                                    User user = info.sender() instanceof Player player ? Users.getUserIfLoadedImmediately(player.getUniqueId()) : null;
                                    if (user == null) {
                                        return List.of();
                                    }

                                    Team team = user.team();
                                    if (team != null) {
                                        return List.of();
                                    }

                                    List<Team> teams = Teams.loaded();

                                    return teams.stream()
                                            .filter(s -> s.invited(user))
                                            .map(Team::name)
                                            .toList();
                                })).executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team != null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.alreadyInTeam, Placeholder.unparsed("team", team.name()));
                                        return;
                                    }

                                    Team joiningTeam = Teams.getTeamIfLoadedImmediately(args.getByClass("team", TeamID.class));
                                    if (joiningTeam == null) {
                                        return;
                                    }

                                    if (!joiningTeam.invited(user)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.invite.notInvited);
                                        return;
                                    }

                                    int memberSize = joiningTeam.members(true).size();
                                    Integer sizeLimit = joiningTeam.first(TeamValues.TEAM_SIZE_LIMIT);
                                    sizeLimit = sizeLimit == null ? Config.defaultTeamSizeLimit : sizeLimit;
                                    if (memberSize + 1 > sizeLimit) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.invite.teamIsFull);
                                        return;
                                    }

                                    joiningTeam.removeInvite(user);
                                    joiningTeam.add(user);
                                    MessageUtils.sendMessage(sender, Language.prefix, Language.invite.inviteAccepted);
                                    joiningTeam.message(StringUtils.format(Language.prefix + Language.invite.inviteAcceptedAnnouncement, Placeholder.unparsed("player", sender.getName())));
                                })
                        )
                )
                .then(new LiteralArgument("ally")
                        .then(new LiteralArgument("accept")
                                .then(TeamArgument.invited("team")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                                return;
                                            }

                                            Team userTeam = user.team();
                                            if (userTeam == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                                return;
                                            }

                                            TeamID teamID = args.getByClass("team", TeamID.class);
                                            if (teamID == null) {
                                                return;
                                            }

                                            if (!user.hasPermission(Permissions.ALLY_ACCEPT)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                                return;
                                            }

                                            Team team = Teams.getTeamIfLoadedImmediately(teamID);
                                            if (team == null || !team.hasOnline()) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.notOnline, Placeholder.unparsed("team", Teams.byId(teamID)));
                                                return;
                                            }

                                            if (userTeam.isAlly(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.alreadyAllies, Placeholder.unparsed("team", team.name()));
                                                return;
                                            }

                                            if (!userTeam.hasAllyRequest(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.notInvited, Placeholder.unparsed("team", team.name()));
                                                return;
                                            }

                                            userTeam.removeAllyRequest(teamID);
                                            team.removeAllyRequest(userTeam.id());

                                            int alliesSize = userTeam.values(TeamValues.ALLIES).size();
                                            Integer sizeLimit = userTeam.first(TeamValues.TEAM_ALLY_LIMIT);
                                            sizeLimit = sizeLimit == null ? Config.defaultAllyLimit : sizeLimit;
                                            if (alliesSize + 1 > sizeLimit) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.allyLimitReached);
                                                return;
                                            }

                                            int otherAlliesSize = team.values(TeamValues.ALLIES).size();
                                            Integer otherSizeLimit = team.first(TeamValues.TEAM_ALLY_LIMIT);
                                            otherSizeLimit = otherSizeLimit == null ? Config.defaultAllyLimit : otherSizeLimit;
                                            if (otherAlliesSize + 1 > otherSizeLimit) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.allyLimitReached);
                                                return;
                                            }

                                            team.addAlly(userTeam.id());
                                            userTeam.addAlly(teamID);
                                            MessageUtils.sendMessage(sender, Language.prefix, Language.ally.inviteAccepted, Placeholder.unparsed("team", team.name()));
                                            userTeam.message(StringUtils.format(Language.prefix + Language.ally.inviteAcceptedAnnouncement, Placeholder.unparsed("player", user.name()), Placeholder.unparsed("team", team.name())));
                                            team.message(StringUtils.format(Language.prefix + Language.ally.inviteAcceptedAnnouncement, Placeholder.unparsed("player", user.name()), Placeholder.unparsed("team", userTeam.name())));
                                        })
                                )
                        )
                        .then(new LiteralArgument("invite")
                                .then(TeamArgument.team("team")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                                return;
                                            }

                                            Team userTeam = user.team();
                                            if (userTeam == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                                return;
                                            }

                                            if (!user.hasPermission(Permissions.ALLY_SEND)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                                return;
                                            }

                                            TeamID teamID = args.getByClass("team", TeamID.class);
                                            if (teamID == null) {
                                                return;
                                            }

                                            Team team = Teams.getTeamIfLoadedImmediately(teamID);
                                            if (team == null || !team.hasOnline()) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.notOnline, Placeholder.unparsed("team", Teams.byId(teamID)));
                                                return;
                                            }

                                            if (userTeam.isAlly(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.alreadyAllies, Placeholder.unparsed("team", team.name()));
                                                return;
                                            }

                                            if (team.hasAllyRequest(userTeam.id())) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.alreadyInvited, Placeholder.unparsed("team", team.name()));
                                                return;
                                            }

                                            if (userTeam.hasAllyRequest(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.hasIncomingRequest, Placeholder.unparsed("team", team.name()));
                                                return;
                                            }

                                            int alliesSize = userTeam.values(TeamValues.ALLIES).size();
                                            Integer sizeLimit = userTeam.first(TeamValues.TEAM_ALLY_LIMIT);
                                            sizeLimit = sizeLimit == null ? Config.defaultAllyLimit : sizeLimit;
                                            if (alliesSize + 1 > sizeLimit) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.ally.allyLimitReached);
                                                return;
                                            }

                                            team.addAllyRequest(userTeam.id());
                                            userTeam.addAllyRequest(teamID);
                                            MessageUtils.sendMessage(sender, Language.prefix, Language.ally.inviteSent, Placeholder.unparsed("team", team.name()));
                                            userTeam.message(StringUtils.format(Language.prefix + Language.ally.inviteSentAnnouncement, Placeholder.unparsed("player", user.name()), Placeholder.unparsed("team", team.name())));
                                            team.message(StringUtils.format(Language.prefix + Language.ally.inviteReceived, Placeholder.unparsed("player", user.name()), Placeholder.unparsed("team", userTeam.name())));
                                        })
                                )
                        )
                        .then(new LiteralArgument("remove")
                                .then(TeamArgument.ally("team")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                                return;
                                            }

                                            Team userTeam = user.team();
                                            if (userTeam == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                                return;
                                            }

                                            if (!user.hasPermission(Permissions.ALLY_REMOVE)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                                return;
                                            }

                                            TeamID teamID = args.getByClass("team", TeamID.class);
                                            if (teamID == null) {
                                                return;
                                            }

                                            Team team = Teams.getTeamIfLoadedImmediately(teamID);
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, "Team is not loaded");
                                                return;
                                            }

                                            if (!userTeam.isAlly(teamID)) {
                                                MessageUtils.sendMessage(sender, Language.prefix, "Not allies!");
                                                return;
                                            }

                                            userTeam.removeAlly(teamID);
                                            team.removeAlly(userTeam.id());

                                            MessageUtils.sendMessage(sender, Language.prefix, Language.ally.removed, Placeholder.unparsed("team", team.name()));
                                            userTeam.message(StringUtils.format(Language.prefix + Language.ally.removedAnnouncement, Placeholder.unparsed("player", user.name()), Placeholder.unparsed("team", team.name())));
                                            team.message(StringUtils.format(Language.prefix + Language.ally.removedAnnouncement, Placeholder.unparsed("player", user.name()), Placeholder.unparsed("team", userTeam.name())));
                                        })
                                )
                        )
                )
                .then(new LiteralArgument("kick")
                        .then(TeamMemberArgument.teamMember("member")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    User other = args.getByClass("member", User.class);
                                    if (other == null) {
                                        return;
                                    }

                                    if (!user.hasPermission(Permissions.KICK, other)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                        return;
                                    }

                                    team.remove(other);
                                    MessageUtils.sendMessage(sender, Language.prefix, Language.success.kicked, Placeholder.unparsed("player", other.name()));
                                })
                        )
                )
                .then(new LiteralArgument("rename")
                        .then(new GreedyStringArgument("name")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    if (!user.equals(team.leader())) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notLeader);
                                        return;
                                    }

                                    String name = args.getByClass("name", String.class);
                                    if (name == null) {
                                        return;
                                    }
                                    name = name.split(" ")[0];

                                    if (Teams.doesNameExist(name)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.alreadyExists);
                                        return;
                                    }

                                    NameValidation validation = Teams.validate(name);
                                    switch (validation) {
                                        case TOO_SHORT ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.tooShort, Placeholder.unparsed("name", name));
                                        case TOO_LONG ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.tooLong, Placeholder.unparsed("name", name));
                                        case BLACKLISTED ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.blacklisted, Placeholder.unparsed("name", name));
                                        case NOT_WHITELISTED ->
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.notWhitelisted, Placeholder.unparsed("name", name));
                                        case VALID -> {
                                            Teams.changeName(team.name(), name);
                                            team.name(name);
                                        }
                                    }
                                })
                        )
                )
                .then(new LiteralArgument("displayname")
                        .then(new StringArgument("name")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    String newName = args.getByClass("name", String.class);

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    if (!user.hasPermission(Permissions.CHANGE_DISPLAY_NAME)) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                        return;
                                    }


                                    Component previous = team.first(TeamValues.TEAM_DISPLAY_NAME);
                                    if (previous == null || newName == null) {
                                        return;
                                    }

                                    Component name = StringUtils.format(newName);
                                    String plain = PlainTextComponentSerializer.plainText().serialize(name);
                                    if (Config.teamName.applyToDisplayName) {
                                        NameValidation validation = Teams.validate(plain);
                                        switch (validation) {
                                            case TOO_SHORT ->
                                                    MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.tooShort, Placeholder.component("name", name));
                                            case TOO_LONG ->
                                                    MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.tooLong, Placeholder.component("name", name));
                                            case BLACKLISTED ->
                                                    MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.blacklisted, Placeholder.component("name", name));
                                            case NOT_WHITELISTED ->
                                                    MessageUtils.sendMessage(sender, Language.prefix, Language.error.teamNaming.notWhitelisted, Placeholder.component("name", name));
                                            case VALID -> {
                                                team.add(TeamValues.TEAM_DISPLAY_NAME, new IdentifiableComponent(name));
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.success.displayNameChanged, Placeholder.component("old-name", previous), Placeholder.component("name", name));
                                                team.message(StringUtils.format(Language.prefix + Language.success.displayNameChangedAnnouncement, Placeholder.unparsed("player", user.name()), Placeholder.component("old-name", previous), Placeholder.component("name", name)));
                                            }
                                        }
                                    }
                                })
                        )
                )
                .then(new LiteralArgument("enderchest")
                        .executesPlayer((sender, args) -> {
                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                            if (user == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                return;
                            }

                            Team team = user.team();
                            if (team == null) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                return;
                            }

                            if (!user.hasPermission(Permissions.ENDER_CHEST_OPEN)) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.noPermission);
                                return;
                            }

                            Inventory enderChest = team.enderChest();
                            if (enderChest == null) {
                                return;
                            }

                            sender.openInventory(enderChest);
                        })
                )
                .then(new LiteralArgument("bank")
                        .then(new LiteralArgument("balance")
                                .executesPlayer((sender, args) -> {
                                    User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                    if (user == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                        return;
                                    }

                                    Team team = user.team();
                                    if (team == null) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                        return;
                                    }

                                    BigDecimal balance = team.first(TeamValues.BANK);
                                    balance = balance == null ? BigDecimal.ZERO : balance;
                                    MessageUtils.sendMessage(sender, Language.prefix, Language.success.bankBalance, Placeholder.unparsed("balance", balance.toPlainString()));
                                })
                        )
                        .then(new LiteralArgument("take")
                                .then(new DoubleArgument("amount")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                                return;
                                            }

                                            Team team = user.team();
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                                return;
                                            }

                                            Double amount = args.getByClass("amount", Double.class);
                                            if (amount == null || amount.isInfinite() || amount.isNaN()) {
                                                return;
                                            }

                                            BigDecimal balance = team.first(TeamValues.BANK);
                                            balance = balance == null ? BigDecimal.ZERO : balance;
                                            BigDecimal newValue = balance.subtract(BigDecimal.valueOf(amount));
                                            if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notEnoughBankBalance, Placeholder.unparsed("amount", String.valueOf(amount)), Placeholder.unparsed("balance", balance.toPlainString()));
                                                return;
                                            }

                                            team.add(TeamValues.BANK, new IdentifiableBigDecimal(newValue));

                                            MessageUtils.sendMessage(sender, Language.prefix, Language.success.bankBalanceTake, Placeholder.unparsed("amount", String.valueOf(amount)), Placeholder.unparsed("balance", balance.toPlainString()));
                                        })
                                )
                        )
                        .then(new LiteralArgument("deposit")
                                .then(new DoubleArgument("amount")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxTeamsAPI.instance().getUserIfLoadedImmediately(sender);
                                            if (user == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.userNotLoaded);
                                                return;
                                            }

                                            Team team = user.team();
                                            if (team == null) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notInTeam);
                                                return;
                                            }

                                            Double amount = args.getByClass("amount", Double.class);
                                            if (amount == null || amount.isInfinite() || amount.isNaN()) {
                                                return;
                                            }

                                            // TODO: Economy hook take
                                            BigDecimal balance = team.first(TeamValues.BANK);
                                            balance = balance == null ? BigDecimal.ZERO : balance;
                                            BigDecimal newValue = balance.add(BigDecimal.valueOf(amount));
                                            if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
                                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notEnoughToDeposit, Placeholder.unparsed("amount", String.valueOf(amount)), Placeholder.unparsed("balance", balance.toPlainString()));
                                                return;
                                            }

                                            team.add(TeamValues.BANK, new IdentifiableBigDecimal(newValue));

                                            MessageUtils.sendMessage(sender, Language.prefix, Language.success.bankBalanceTake, Placeholder.unparsed("amount", String.valueOf(amount)), Placeholder.unparsed("balance", balance.toPlainString()));
                                        })
                                )
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
                                    MessageUtils.sendMessage(sender, Language.prefix, "<green>You are running <white>AxTeams</white> version <white><version></white> on <white><implementation></white> version <white><implementation-version></white> (Implementing API version <white><api-version></white>)",
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
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.reload.success, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)));
                                    } else {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.reload.fail, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)), Placeholder.unparsed("files", String.join(", ", failed)));
                                    }
                                })
                        )
                )
                .register();
    }
}
