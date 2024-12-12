package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;

public final class ConfigGui extends GuiBase {

    public ConfigGui(User user, Config config) {
        super(user, config, false);
    }

    @Override
    public void open() {
    }
}
