package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;

public final class GroupEditGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("group-editor.yml").toFile());
    private final Group group;

    public GroupEditGui(User user, Group group) {
        super(user, config, false, true);
        this.group = group;
    }

    @Override
    public void open() {
        this.populate();
    }

    @Override
    public void populate() {
        super.populate();

    }
}
