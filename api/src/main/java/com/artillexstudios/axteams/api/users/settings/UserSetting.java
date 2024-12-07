package com.artillexstudios.axteams.api.users.settings;

import com.artillexstudios.axteams.api.users.User;
import com.google.common.base.Preconditions;

import java.util.Objects;

public abstract class UserSetting<T> {
    private final String id;
    private final T def;
    private T value;

    public UserSetting(String id, T def) {
        Preconditions.checkNotNull(def, "Found null default value for UserSetting with id: " + id + "!");
        this.id = id;
        this.def = def;
    }

    public T read(User user) {
        return user.settingsRepository().read(this);
    }

    public void write(User user, T value) {
        user.settingsRepository().write(this, value);
    }

    public T defaultValue() {
        return this.def;
    }

    public String id() {
        return this.id;
    }

    public abstract String asString(T value);

    public abstract T fromString(String string);

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof UserSetting<?> that)) {
            return false;
        }

        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
