package com.artillexstudios.axteams.users;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.StringGroup;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class User implements com.artillexstudios.axteams.api.users.User {
    private final OfflinePlayer player;
    private final int id;
    private Team team;
    private String textures;
    private Group group;
    private long lastOnline;

    public User(int id, OfflinePlayer player, Team team, String texture, Group group, long lastOnline) {
        this.id = id;
        this.player = player;
        this.team = team;
        this.textures = texture;
        this.group = group;
        this.lastOnline = lastOnline;
    }

    @Override
    public void team(Team team) {
        if (this.group instanceof StringGroup) {
            this.group = team.values(TeamValues.GROUPS)
                    .stream()
                    .filter(gr -> gr.name().equals(this.group.name()))
                    .findFirst()
                    .orElse(this.group); // TODO: get default group or owner group if the user is the leader
        }

        if (team == null) {
            this.group = Group.ofString("");
        }

        this.team = team;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Nullable
    @Override
    public Team team() {
        return this.team;
    }

    @Override
    public OfflinePlayer player() {
        return this.player;
    }

    @Override
    public long lastOnline() {
        return this.lastOnline;
    }

    @Override
    public String textures() {
        return this.textures;
    }

    @Override
    public void group(Group group) {
        this.group = group;
    }

    @Override
    public Group group() {
        return this.group;
    }

    @Override
    public void markUnsaved() {
        Users.markUnsaved(this);
    }

    @Override
    public void message(String message) {
        Player player = this.player().getPlayer();
        if (player == null) {
            return;
        }

        NMSHandlers.getNmsHandler().sendMessage(player, StringUtils.format(message));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) {
            return false;
        }

        return this.id == user.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
