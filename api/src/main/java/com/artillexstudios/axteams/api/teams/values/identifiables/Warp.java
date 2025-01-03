package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.State;
import org.bukkit.Location;

import java.util.Objects;

public final class Warp implements Identifiable<Warp> {
    private final String name;
    private final Location location;
    private final String password;
    private State state = State.UNSAVED;
    private int id;

    public Warp(int id, String name, Location location, String password) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.password = password;

        if (this.id != 0) {
            this.state = State.SAVED;
        }
    }

    public Warp(String name, Location location, String password) {
        this(0, name, location, password);
    }

    public String name() {
        return this.name;
    }

    public Location location() {
        return this.location.clone();
    }

    public String password() {
        return this.password;
    }

    @Override
    public Warp get() {
        return this;
    }

    @Override
    public void set(Warp value) {

    }

    @Override
    public State state() {
        return this.state;
    }

    @Override
    public void state(State state) {
        this.state = state;
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
    public boolean equals(Object o) {
        if (!(o instanceof Warp warp)) {
            return false;
        }

        return Objects.equals(this.name, warp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}
