package com.artillexstudios.axteams.placeholders;

import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Language;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;

public enum PlaceholderRegistry {
    INSTANCE;

    public void register() {
        Placeholders.registerTransformer(OfflinePlayer.class, User.class, player -> {
            return AxTeamsAPI.instance().getUserIfLoadedImmediately(player);
        });

        Placeholders.register("team_name", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "Do I know you?";
            }

            Team team = user.team();
            if (team == null) {
                return "No team";
            }

            return team.name();
        }, ParseContext.PLACEHOLDER_API);

        Placeholders.register("team_display_name", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "Do I know you?";
            }

            Team team = user.team();
            if (team == null) {
                return "No team";
            }

            Component first = team.first(TeamValues.TEAM_DISPLAY_NAME);
            if (first == null) {
                return team.name();
            }

            return MiniMessage.miniMessage().serialize(first);
        });

        Placeholders.register("member_count", ctx -> {
            Team team = ctx.resolve(Team.class);
            if (team == null) {
                return "0";
            }

            return Integer.toString(team.members(true).size());
        });

        Placeholders.register("texture", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "";
            }

            return user.textures();
        });

        Placeholders.register("group_display_name", ctx -> {
            if (ctx.has(Group.class)) {
                Group group = ctx.raw(Group.class);
                if (group == null) {
                    return "---";
                }

                return MiniMessage.miniMessage().serialize(group.displayName());
            }

            User user = ctx.resolve(User.class);
            if (user == null) {
                return "";
            }

            return MiniMessage.miniMessage().serialize(user.group().displayName());
        });

        Placeholders.register("group_name", ctx -> {
            if (ctx.has(Group.class)) {
                Group group = ctx.raw(Group.class);
                if (group == null) {
                    return "---";
                }

                return group.name();
            }

            User user = ctx.resolve(User.class);
            if (user == null) {
                return "---";
            }

            return user.group().name();
        });

        Placeholders.register("player", ctx -> {
            User user = ctx.resolve(User.class);
            if (user == null) {
                return "";
            }

            return user.player().getName();
        });

        Placeholders.register("permission", ctx -> {
            Permission permission = ctx.resolve(Permission.class);
            if (permission == null) {
                return "";
            }

            return permission.display();
        });

        Placeholders.register("level", ctx -> {
            if (ctx.has(Team.class)) {
                Team team = ctx.raw(Team.class);
                if (team == null) {
                    return "0";
                }

                return Integer.toString(team.members(true).size());
            }

            User user = ctx.resolve(User.class);
            Team team = user.team();
            if (team == null) {
                return Language.placeholder.noTeam;
            }

            Integer level = team.first(TeamValues.LEVEL);
            return level == null ? "0" : level.toString();
        });

        Placeholders.register("message", ctx -> {
            return ctx.resolve(String.class);
        });
    }
}
