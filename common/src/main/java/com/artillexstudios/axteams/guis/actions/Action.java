package com.artillexstudios.axteams.guis.actions;

import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;

import java.util.Objects;

public abstract class Action<T> {
    private final String id;

    public Action(String id) {
        this.id = id;
    }

    public abstract T evaluate(String input);

    public abstract void execute(User user, GuiBase base, T value);

    public String id() {
        return this.id;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Action<?> action)) {
            return false;
        }

        return Objects.equals(this.id, action.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
