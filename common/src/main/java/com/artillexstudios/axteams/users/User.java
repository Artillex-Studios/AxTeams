package com.artillexstudios.axteams.users;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.IdGroup;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.collections.CircularLinkedSet;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.guis.GuiBase;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class User implements com.artillexstudios.axteams.api.users.User {
    private final CircularLinkedSet<Supplier<GuiBase>> guis = new CircularLinkedSet<>(10);
    private final UserSettingsRepository repository = new UserSettingsRepository(this);
    private final OfflinePlayer player;
    private final int id;
    private Team team;
    private String textures;
    private Group group;
    private Player onlinePlayer;
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
        if (this.group instanceof IdGroup) {
            if (Config.DEBUG) {
                LogUtils.debug("Looking for group with id {}.", this.group.id());
            }

            List<Group> groups = team.values(TeamValues.GROUPS);
            if (Config.DEBUG) {
                LogUtils.debug("Loaded groups in team: {}", groups);
            }
            Optional<Group> optional = groups
                    .stream()
                    .filter(group -> group.id() == this.group.id())
                    .findFirst();

            if (optional.isPresent()) {
                this.group = optional.get();
            } else {
                this.group = groups.stream()
                        .filter(gr -> team.leader().equals(this) ? gr.priority() == Group.OWNER_PRIORITY : gr.priority() == Group.DEFAULT_PRIORITY)
                        .findFirst()
                        .orElseThrow();
            }
        }

        if (team == null) {
            this.group = Group.ofId(0);
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
        this.markUnsaved();
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
    public void message(Component message) {
        Player player = this.onlinePlayer;
        if (player == null) {
            return;
        }

        NMSHandlers.getNmsHandler().sendMessage(player, message);
    }

    @Override
    public Player onlinePlayer() {
        return this.onlinePlayer;
    }

    @Override
    public String name() {
        return this.onlinePlayer == null ? this.player().getName() : this.onlinePlayer.getName();
    }

    @Override
    public com.artillexstudios.axteams.api.users.settings.UserSettingsRepository settingsRepository() {
        return this.repository;
    }

    public void onlinePlayer(Player player) {
        this.onlinePlayer = player;
    }

    public CircularLinkedSet<Supplier<GuiBase>> guis() {
        return this.guis;
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
