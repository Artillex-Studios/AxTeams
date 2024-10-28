package com.artillexstudios.axteams.api.teams;

import net.kyori.adventure.text.Component;

import java.util.List;

public final class StringGroup extends Group {
    private final String name;

    public StringGroup(String name) {
        super(0, 0, null, null, null);
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Component displayName() {
        return Component.empty();
    }

    @Override
    public List<Permission> permissions() {
        return List.of();
    }
}
