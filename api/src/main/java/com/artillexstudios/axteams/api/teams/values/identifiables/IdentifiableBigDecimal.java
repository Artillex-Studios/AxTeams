package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.State;

import java.math.BigDecimal;
import java.util.Objects;

public final class IdentifiableBigDecimal implements Identifiable<BigDecimal> {
    private BigDecimal value;
    private State state = State.UNSAVED;
    private int id;

    public IdentifiableBigDecimal(int id, BigDecimal value) {
        this.id = id;
        this.value = value;

        if (this.id != 0) {
            this.state = State.SAVED;
        }
    }

    public IdentifiableBigDecimal(BigDecimal value) {
        this(0, value);
    }

    @Override
    public BigDecimal get() {
        return this.value;
    }

    @Override
    public void set(BigDecimal value) {
        this.value = value;
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
        if (!(o instanceof IdentifiableBigDecimal that)) {
            return false;
        }

        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
