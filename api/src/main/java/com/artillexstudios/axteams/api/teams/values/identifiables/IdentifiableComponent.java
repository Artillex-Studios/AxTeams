package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.State;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Objects;

public final class IdentifiableComponent implements Identifiable<Component> {
    private Component component;
    private State state = State.UNSAVED;
    private int id;

    public IdentifiableComponent(int id, Component component) {
        this.id = id;
        this.component = component;

        if (this.id != 0) {
            this.state = State.SAVED;
        }
    }

    public IdentifiableComponent(Component component) {
        this(0, component);
    }

    @Override
    public Component get() {
        return this.component;
    }

    @Override
    public void set(Component value) {
        this.component = value;
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
        return MiniMessage.miniMessage().serialize(this.component);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IdentifiableComponent that)) {
            return false;
        }

        return Objects.equals(this.component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.component);
    }
}
