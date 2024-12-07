package com.artillexstudios.axteams.api.users.settings;

public interface UserSettingsRepository {

    <T> T read(UserSetting<T> setting);

    <T> void write(UserSetting<T> setting, T value);
}
