package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

        OfflinePlayer offlinePlayer = this.user().player();
        if (com.artillexstudios.axteams.config.Config.DEBUG) {
            LogUtils.debug("Open called for user: {}", offlinePlayer.getName());
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            LogUtils.warn("Attempted to open group editor gui for offline player {} ({})", offlinePlayer.getName(), offlinePlayer.getUniqueId());
            return;
        }

        this.gui().open(player);
    }

    @Override
    public void populate() {
        super.populate();

    }
}
