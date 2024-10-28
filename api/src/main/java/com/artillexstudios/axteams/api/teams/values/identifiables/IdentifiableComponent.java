package com.artillexstudios.axteams.api.teams.values.identifiables;

import com.artillexstudios.axteams.api.teams.values.Identifiable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Objects;

public final class IdentifiableComponent implements Identifiable<Component> {
    private Component component;
    private int id;

    public IdentifiableComponent(int id, Component component) {
        this.id = id;
        this.component = component;
    }

    public IdentifiableComponent(Component component) {
        this(DEFAULT, component);
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
