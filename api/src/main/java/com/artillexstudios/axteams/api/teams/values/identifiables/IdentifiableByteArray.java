package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.State;

import java.util.Arrays;

public final class IdentifiableByteArray implements Identifiable<byte[]> {
    private byte[] bytes;
    private State state = State.UNSAVED;
    private int id;

    public IdentifiableByteArray(int id, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;

        if (this.id != 0) {
            this.state = State.SAVED;
        }
    }

    public IdentifiableByteArray(byte[] bytes) {
        this(0, bytes);
    }

    @Override
    public byte[] get() {
        return this.bytes;
    }

    @Override
    public void set(byte[] value) {
        this.bytes = value;
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
    public String toString() {
        return Arrays.toString(this.bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IdentifiableByteArray that)) {
            return false;
        }

        return Arrays.equals(this.bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }
}
