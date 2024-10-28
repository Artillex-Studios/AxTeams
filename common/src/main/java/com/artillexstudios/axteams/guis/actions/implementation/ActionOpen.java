package com.artillexstudios.axteams.guis.actions.implementation;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.Guis;
import com.artillexstudios.axteams.guis.actions.Action;

public final class ActionOpen extends Action<String> {

    public ActionOpen() {
        super("open");
    }

    @Override
    public String evaluate(String input) {
        return input;
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        GuiBase guiBase = Guis.fetch(value, user);
        if (guiBase == null) {
            LogUtils.warn("No gui named {} is present!", value);
            return;
        }

        guiBase.open();
    }
}
