package com.artillexstudios.axteams.api.users.settings;

public final class BooleanUserSetting extends UserSetting<Boolean> {

    public BooleanUserSetting(String id, Boolean def) {
        super(id, def);
    }

    @Override
    public String asString(Boolean value) {
        return value.toString();
    }

    @Override
    public Boolean fromString(String string) {
        return Boolean.valueOf(string);
    }
}
