package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;

import java.math.BigDecimal;
import java.util.Objects;

public final class IdentifiableBigDecimal implements Identifiable<BigDecimal> {
    private BigDecimal value;
    private int id;

    public IdentifiableBigDecimal(int id, BigDecimal value) {
        this.id = id;
        this.value = value;
    }

    public IdentifiableBigDecimal(BigDecimal value) {
        this(DEFAULT, value);
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
