package com.artillexstudios.axteams.guis.actions.implementation;

import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.actions.Action;
import org.bukkit.Bukkit;

public final class ActionConsoleCommand extends Action<String> {

    public ActionConsoleCommand() {
        super("console");
    }

    @Override
    public String evaluate(String input) {
        return input;
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        Scheduler.get().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholders.parse(value, Context.builder(ParseContext.INTERNAL, ResolutionType.ONLINE).add(User.class, user))));
    }
}
