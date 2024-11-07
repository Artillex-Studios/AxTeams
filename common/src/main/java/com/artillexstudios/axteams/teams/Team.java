package com.artillexstudios.axteams.teams;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableInteger;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.collections.ExpiringList;
import com.artillexstudios.axteams.config.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class Team implements com.artillexstudios.axteams.api.teams.Team {
    private final ConcurrentHashMap<TeamValue<?, ?>, List<Identifiable<?>>> data = new ConcurrentHashMap<>();
    private final List<User> members = new ObjectArrayList<>();
    private final ExpiringList<User> invited = new ExpiringList<>(Duration.ofMinutes(5).toMillis());
    private final ExpiringList<TeamID> allyRequests = new ExpiringList<>(Duration.ofMinutes(5).toMillis());
    private final TeamID teamID;
    private User leader;
    private String name;

    public Team(TeamID teamID, User leader, String name) {
        this.teamID = teamID;
        this.leader = leader;
        this.name = name;
    }

    @Override
    public TeamID id() {
        return this.teamID;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void name(String name) {
        this.name = name;
        this.markUnsaved();
    }

    @Override
    public void add(User user) {
        this.members.add(user);
        user.team(this);
        this.markUnsaved();
    }

    @Override
    public void remove(User user) {
        this.members.remove(user);
        user.team(null);
        this.markUnsaved();
    }

    @Override
    public User leader() {
        return this.leader;
    }

    @Override
    public void leader(User user) {
        this.leader = user;
    }

    @Override
    public List<User> members(boolean includeOwner) {
        List<User> members = new ArrayList<>(this.members);
        if (includeOwner) {
            members.add(this.leader);
        }

        return members;
    }

    @Override
    public boolean hasOnline() {
        for (User member : members(true)) {
            if (member.onlinePlayer() != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> List<T> rawValues(Z type) {
        List<T> list = (List<T>) this.data.get(type);
        List<T> returning = new ArrayList<>(list == null ? 1 : list.size());
        if (list != null && !list.isEmpty()) {
            for (T identifiable : list) {
                if (identifiable == null) {
                    continue;
                }

                if (identifiable.id() == Identifiable.DELETED) {
                    continue;
                }

                returning.add(identifiable);
            }
        }

        if (returning.isEmpty() && type.defaultValue() != null) {
            returning.add(type.defaultValue());
        }

        return returning;
    }

    public void loadAll(TeamValue<?, ?> type, List<Identifiable<?>> values) {
        data.computeIfAbsent(type, val -> new ArrayList<>()).addAll(values);
    }

    @Override
    public <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> void add(Z type, T value) {
        List<Identifiable<Y>> items = (List<Identifiable<Y>>) (Object) this.data.computeIfAbsent(type, val -> new ArrayList<>());
        if (type.single()) {
            if (Config.DEBUG) {
                LogUtils.debug("Add for type: {}", type.id());
            }

            if (items.size() == 1 && items.get(0) != null) {
                if (Config.DEBUG) {
                    LogUtils.debug("Replacing!");
                }
                Identifiable<Y> val = items.get(0);
                val.set(value.get());
            } else {
                if (Config.DEBUG) {
                    LogUtils.debug("Adding!");
                }
                items.add(value);
            }
        } else {
            if (Config.DEBUG) {
                LogUtils.debug("Adding, as it's not single!");
            }
            items.add(value);
        }
        markUnsaved();
    }

    public <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> void remove(Z type, T value) {
        List<T> list = (List<T>) this.data.get(type);
        if (list != null) {
            list.remove(value);
            markUnsaved();
        }
    }

    @Override
    public void markUnsaved() {
        Teams.markUnsaved(this);
    }

    @Override
    public CompletableFuture<Void> disband() {
        return Teams.disband(this);
    }

    @Override
    public void clearDeleted() {
        for (Map.Entry<TeamValue<?, ?>, List<Identifiable<?>>> entry : this.data.entrySet()) {
            entry.getValue().removeIf(next -> next.id() == Identifiable.DELETED);
        }
    }

    @Override
    public void invite(User user) {
        this.invited.add(user);
    }

    @Override
    public void removeInvite(User user) {
        this.invited.remove(user);
    }

    @Override
    public boolean invited(User user) {
        return this.invited.contains(user);
    }

    @Override
    public void addAlly(TeamID teamID) {
        this.add(TeamValues.ALLIES, new IdentifiableInteger(teamID.id()));
    }

    @Override
    public void removeAlly(TeamID teamID) {
        this.remove(TeamValues.ALLIES, new IdentifiableInteger(teamID.id()));
    }

    @Override
    public void addAllyRequest(TeamID teamID) {
        this.allyRequests.add(teamID);
    }

    @Override
    public void removeAllyRequest(TeamID teamID) {
        this.allyRequests.remove(teamID);
    }

    @Override
    public boolean isAlly(TeamID teamID) {
        if (this.teamID.equals(teamID)) {
            return true;
        }

        return this.values(TeamValues.ALLIES).contains(teamID.id());
    }

    @Override
    public boolean hasAllyRequest(TeamID teamID) {
        return this.allyRequests.contains(teamID);
    }

    @Override
    public void message(Component message) {
        for (User member : this.members(true)) {
            member.message(message);
        }
    }
}
