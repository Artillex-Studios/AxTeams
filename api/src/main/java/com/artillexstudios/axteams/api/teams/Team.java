package com.artillexstudios.axteams.api.teams;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.users.User;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

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

    List<User> members(Predicate<User> predicate, boolean includeOwner);

    default boolean hasOnline() {
        return !this.members(member -> member.onlinePlayer() != null, true).isEmpty();
    }

    default <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> List<T> rawValues(Z type) {
        return this.rawValues(type, false);
    }

    <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> List<T> rawValues(Z type, boolean all);

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

    <Y, T extends Identifiable<Y>, Z extends TeamValue<Y, T>> void remove(Z type, T value);

    void markUnsaved();

    CompletableFuture<Void> disband();

    void clearDeleted();

    default boolean hasPermission(User user, Permission permission) {
        if (user == this.leader()) {
            return true;
        }

        return user.group().permissions().contains(Permissions.ALL) || user.group().permissions().contains(permission);
    }

    default boolean hasPermission(User user, User other, Permission permission) {
        return this.hasPermission(user, other.group(), permission);
    }

    default boolean hasPermission(User user, Group other, Permission permission) {
        if (user == this.leader()) {
            return true;
        }

        return this.hasPermission(user, permission) && user.group().priority() > other.priority();
    }

    void invite(User user);

    void removeInvite(User user);

    boolean invited(User user);

    void addAlly(TeamID teamID);

    void removeAlly(TeamID teamID);

    void addAllyRequest(TeamID teamID);

    void removeAllyRequest(TeamID teamID);

    boolean isAlly(TeamID teamID);

    boolean hasAllyRequest(TeamID teamID);

    List<TeamID> allyRequest();

    void message(Component message);

    Inventory enderChest();
}
