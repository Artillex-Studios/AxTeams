package com.artillexstudios.axteams.api.teams;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.users.User;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Team {

    TeamID id();

    String name();

    void name(String name);

    User leader();

    void leader(User user);

    void add(User user);

    void remove(User user);

    default List<User> members() {
        return this.members(false);
    }

    List<User> members(boolean includeOwner);

    boolean hasOnline();

    <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> List<T> rawValues(Z type);

    default <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> List<Y> values(Z type) {
        List<T> values = this.rawValues(type);
        List<Y> returning = new ArrayList<>(values.size());

        if (values.isEmpty()) {
            return returning;
        }

        for (T value : values) {
            if (value == null) {
                continue;
            }

            returning.add(value.get());
        }

        return returning;
    }

    @Nullable
    default <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> Y first(Z type) {
        List<T> values = this.rawValues(type);
        if (values.isEmpty()) {
            return null;
        }

        return values.get(0).get();
    }

    <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> void add(Z type, T value);

    void markUnsaved();

    CompletableFuture<Void> disband();

    void clearDeleted();

    boolean hasPermission(User user, Permission permission);

    boolean hasPermission(User user, User other, Permission permission);

    void invite(User user);

    void removeInvite(User user);

    boolean invited(User user);

    void addAlly(TeamID teamID);

    void removeAlly(TeamID teamID);

    void addAllyRequest(TeamID teamID);

    void removeAllyRequest(TeamID teamID);

    boolean isAlly(TeamID teamID);

    boolean hasAllyRequest(TeamID teamID);

    void message(String message);
}
