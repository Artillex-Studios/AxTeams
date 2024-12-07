package com.artillexstudios.axteams.guis;

import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.implementation.ConfigGui;
import com.artillexstudios.axteams.guis.implementation.GroupsGui;
import com.artillexstudios.axteams.guis.implementation.MainGui;
import com.artillexstudios.axteams.guis.implementation.UsersGui;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Guis {
    private static final File GUIS_FOLDER = com.artillexstudios.axteams.utils.FileUtils.PLUGIN_DIRECTORY.resolve("guis").toFile();
    private static final Map<String, Function<User, GuiBase>> guis = new HashMap<>();

    public static void loadAll() {
        guis.clear();
        guis.put("main", MainGui::new);
        guis.put("users", UsersGui::new);
        guis.put("groups", GroupsGui::new);

        FileUtils.listFiles(GUIS_FOLDER, new String[]{"yaml", "yml"}, true).stream()
                .filter(file -> !guis.containsKey(FilenameUtils.getBaseName(file.getName())))
                .forEach(file -> guis.put(FilenameUtils.getBaseName(file.getName()), ConfigGui::new));
    }

    public static GuiBase fetch(String name, User user) {
        Function<User, GuiBase> gui = guis.get(name);
        if (gui == null) {
            return null;
        }

        return gui.apply(user);
    }
}
