package com.artillexstudios.axteams.guis.actions.implementation;

import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.actions.Action;
import org.bukkit.entity.Player;

public final class ActionPlayerCommand extends Action<String> {

    public ActionPlayerCommand() {
        super("player");
    }

    @Override
    public String evaluate(String input) {
        return input;
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        Player player = user.onlinePlayer();
        if (player == null) {
            LogUtils.warn("Tried to execute command as {}, who is offline!", user.player().getName());
            return;
        }

        player.performCommand(Placeholders.parse(value, Context.builder(ParseContext.INTERNAL, ResolutionType.ONLINE).add(User.class, user)));
    }
}
