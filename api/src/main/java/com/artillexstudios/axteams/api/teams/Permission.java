package com.artillexstudios.axteams.api.teams;

import java.util.Objects;

public final class Permission {
    private final String permission;
    private String display;

    public Permission(String permission, String display) {
        this.permission = permission;
        this.display = display;
    }

    public String permission() {
        return this.permission;
    }

    public String display() {
        return this.display;
    }

    public void display(String display) {
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Permission that)) {
            return false;
        }

        return Objects.equals(this.permission, that.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.permission);
    }
}
