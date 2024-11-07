package com.artillexstudios.axteams.api.teams;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import net.kyori.adventure.text.Component;

import java.util.List;

public class Group implements Identifiable<Group> {
    public static final int OWNER_PRIORITY = 10000;
    public static final int DEFAULT_PRIORITY = -1;
    private int id;
    private int priority;
    private String name;
    private Component displayName;
    private List<Permission> permissions;

    public Group(int id, int priority, String name, Component displayName, List<Permission> permissions) {
        this.id = id;
        this.priority = priority;
        this.name = name;
        this.displayName = displayName;
        this.permissions = permissions;
    }

    public Group(int priority, String name, Component displayName, List<Permission> permissions) {
        this(DEFAULT, priority, name, displayName, permissions);
    }

    public static Group ofId(int id) {
        return new IdGroup(id);
    }

    public int priority() {
        return this.priority;
    }

    public void priority(int priority) {
        this.priority = priority;
    }

    public String name() {
        return this.name;
    }

    public void name(String name) {
        this.name = name;
    }

    public Component displayName() {
        return this.displayName;
    }

    public void displayName(Component displayName) {
        this.displayName = displayName;
    }

    public List<Permission> permissions() {
        return this.permissions;
    }

    @Override
    public Group get() {
        return this;
    }

    @Override
    public void set(Group value) {

    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public void id(int id) {
        this.id = id;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Group group)) return false;

        return this.id == group.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
