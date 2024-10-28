package com.artillexstudios.axteams.guis;

import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.implementation.GroupsGui;
import com.artillexstudios.axteams.guis.implementation.MainGui;
import com.artillexstudios.axteams.guis.implementation.UsersGui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Guis {
    private static final Map<String, Function<User, GuiBase>> guis = new HashMap<>();

    public static void loadAll() {
        guis.clear();
        guis.put("main", MainGui::new);
        guis.put("users", UsersGui::new);
        guis.put("groups", GroupsGui::new);

        // TODO: Load other files
    }

    public static GuiBase fetch(String name, User user) {
        Function<User, GuiBase> gui = guis.get(name);
        if (gui == null) {
            return null;
        }

        return gui.apply(user);
    }
}
