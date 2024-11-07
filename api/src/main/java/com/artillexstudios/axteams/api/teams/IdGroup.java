package com.artillexstudios.axteams.api.teams;

import net.kyori.adventure.text.Component;

import java.util.List;

public final class IdGroup extends Group {
    private final int id;

    public IdGroup(int id) {
        super(id, 0, null, null, null);
        this.id = id;
    }

    @Override
    public String name() {
        return "";
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
