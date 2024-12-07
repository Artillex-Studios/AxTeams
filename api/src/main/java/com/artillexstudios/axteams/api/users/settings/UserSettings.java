package com.artillexstudios.axteams.api.users.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UserSettings {
    private static final Map<String, UserSetting<?>> settings = new HashMap<>();
    private static final Collection<UserSetting<?>> values = Collections.unmodifiableCollection(settings.values());
    public static final UserSetting<Boolean> ALLY_CHAT_TOGGLED = register(new BooleanUserSetting("ally_chat_toggled", false));
    public static final UserSetting<Boolean> TEAM_CHAT_TOGGLED = register(new BooleanUserSetting("team_chat_toggled", false));

    public static <T> UserSetting<T> register(UserSetting<T> userSetting) {
        settings.put(userSetting.id(), userSetting);
        return userSetting;
    }

    public static <T> UserSetting<T> parse(String id) {
        return (UserSetting<T>) settings.get(id);
    }

    public static Collection<UserSetting<?>> settings() {
        return values;
    }
}
