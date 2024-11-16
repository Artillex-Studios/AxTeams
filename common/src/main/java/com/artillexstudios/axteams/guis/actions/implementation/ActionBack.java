package com.artillexstudios.axteams.guis.actions.implementation;

import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.collections.CircularLinkedSet;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.actions.Action;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public final class ActionBack extends Action<String> {

    public ActionBack() {
        super("back");
    }

    @Override
    public String evaluate(String input) {
        return "";
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        com.artillexstudios.axteams.users.User u = (com.artillexstudios.axteams.users.User) user;
        CircularLinkedSet<Supplier<GuiBase>> guis = u.guis();
        if (guis.isEmpty() || guis.size() == 1) {
            user.onlinePlayer().closeInventory();
            return;
        }

        Supplier<GuiBase> last = guis.removeLast();
        if (last == null) {
            return;
        }

        // TODO: Figure out why it breaks sometimes
        try {
            GuiBase guiBase = guis.last().get();
            if (guiBase != null) {
                guiBase.open();
            } else {
                user.onlinePlayer().closeInventory();
            }
        } catch (NoSuchElementException exception) {
            guis.clear();
            user.onlinePlayer().closeInventory();
        }
    }
}
