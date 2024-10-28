package com.artillexstudios.axteams.guis.actions.implementation;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.actions.Action;
import org.bukkit.entity.Player;

public final class ActionClose extends Action<String> {

    public ActionClose() {
        super("close");
    }

    @Override
    public String evaluate(String input) {
        return "";
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        Player player = user.player().getPlayer();
        if (player == null) {
            LogUtils.warn("Tried to close inventory for {}, who is offline!", user.player().getName());
            return;
        }

        player.closeInventory();
    }
}
