package com.artillexstudios.axteams.api.teams;

import net.kyori.adventure.text.Component;

import java.util.List;

public final class IdGroup extends Group {

    public IdGroup(int id) {
        super(id, 0, null, null, null);
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
