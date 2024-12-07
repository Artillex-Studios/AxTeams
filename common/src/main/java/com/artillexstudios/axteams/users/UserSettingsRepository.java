package com.artillexstudios.axteams.users;

import com.artillexstudios.axteams.api.users.settings.UserSetting;
import com.artillexstudios.axteams.api.users.settings.UserSettings;

import java.util.HashMap;

public final class UserSettingsRepository implements com.artillexstudios.axteams.api.users.settings.UserSettingsRepository {
    private final HashMap<UserSetting<?>, Object> settings = new HashMap<>();
    private final User user;

    public UserSettingsRepository(User user) {
        this.user = user;
    }

    @Override
    public <T> T read(UserSetting<T> setting) {
        T value = (T) this.settings.get(setting);
        if (value == null) {
            value = setting.defaultValue();
            this.settings.put(setting, value);
        }

        return value;
    }

    @Override
    public <T> void write(UserSetting<T> setting, T value) {
        this.settings.put(setting, value);
        this.user.markUnsaved();
    }

    public String save() {
        StringBuilder stringBuilder = new StringBuilder();
        for (UserSetting setting : UserSettings.settings()) {
            Object value = this.read(setting);
            String saved = setting.asString(value);
            stringBuilder.append(setting.id().length())
                    .append('|')
                    .append(setting.id())
                    .append(saved.length())
                    .append('|')
                    .append(saved);
        }

        return stringBuilder.toString();
    }

    public void load(String string) {
        int cursor = 0;
        while (cursor < string.length()) {
            int delimiterPos = string.indexOf('|', cursor);
            if (delimiterPos == -1) {
                throw new IllegalStateException();
            }

            int keyLength = Integer.parseInt(string.substring(cursor, delimiterPos));
            cursor = delimiterPos + 1;
            String key = string.substring(cursor, cursor + keyLength);
            cursor += keyLength;

            delimiterPos = string.indexOf('|', cursor);
            if (delimiterPos == -1) {
                throw new IllegalStateException();
            }

            int valueLength = Integer.parseInt(string.substring(cursor, delimiterPos));
            cursor = delimiterPos + 1;
            String value = string.substring(cursor, cursor + valueLength);
            cursor += valueLength;

            UserSetting<?> setting = UserSettings.parse(key);
            if (setting == null) {
                continue;
            }

            this.settings.put(setting, setting.fromString(value));
        }
    }
}
