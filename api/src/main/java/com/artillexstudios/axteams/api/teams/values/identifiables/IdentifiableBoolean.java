package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;

import java.util.Objects;

public final class IdentifiableBoolean implements Identifiable<Boolean> {
    private Boolean value;
    private int id;

    public IdentifiableBoolean(int id, Boolean value) {
        this.id = id;
        this.value = value;
    }

    public IdentifiableBoolean(Boolean value) {
        this(DEFAULT, value);
    }

    @Override
    public Boolean get() {
        return this.value;
    }

    @Override
    public void set(Boolean value) {
        this.value = value;
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
        if (!(o instanceof IdentifiableBoolean that)) {
            return false;
        }

        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
