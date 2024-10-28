package com.artillexstudios.axteams.guis.actions.implementation;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.actions.Action;
import org.bukkit.entity.Player;

public final class ActionMessage extends Action<String> {

    public ActionMessage() {
        super("message");
    }

    @Override
    public String evaluate(String input) {
        return input;
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        Player player = user.player().getPlayer();
        if (player == null) {
            LogUtils.warn("Tried to send message to {}, who is offline!", user.player().getName());
            return;
        }

        NMSHandlers.getNmsHandler().sendMessage(player, StringUtils.format(value, Placeholders.asMap(Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, user))));
    }
}
