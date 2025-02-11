package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.State;
import org.bukkit.Location;

import java.util.Objects;

public final class IdentifiableLocation implements Identifiable<Location> {
    private Location location;
    private State state = State.UNSAVED;
    private int id;

    public IdentifiableLocation(int id, Location location) {
        this.id = id;
        this.location = location;

        if (this.id != 0) {
            this.state = State.SAVED;
        }
    }

    public IdentifiableLocation(Location location) {
        this(0, location);
    }

    @Override
    public Location get() {
        return this.location.clone();
    }

    @Override
    public void set(Location value) {
        this.location = value;
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
        if (!(o instanceof IdentifiableLocation that)) {
            return false;
        }

        return Objects.equals(this.location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.location);
    }
}
