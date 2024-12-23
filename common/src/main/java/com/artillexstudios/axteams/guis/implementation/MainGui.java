package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import com.artillexstudios.axteams.utils.IdentifiableSupplier;
import org.bukkit.entity.Player;

public final class MainGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("main.yml").toFile());

    public MainGui(User user) {
        super(user, config, false);
        ((com.artillexstudios.axteams.users.User) this.user()).guis().offer(new IdentifiableSupplier<>(MainGui.class) {
            @Override
            public GuiBase get() {
                return new MainGui(user);
            }
        });
    }

    @Override
    public void open() {
        if (com.artillexstudios.axteams.config.Config.debug) {
            LogUtils.debug("Open called for user: {}", this.user().name());
        }

        Player player = this.user().onlinePlayer();
        if (player == null) {
            LogUtils.warn("Attempted to open main gui for offline player {} ({})", this.user().name(), this.user().player().getUniqueId());
            return;
        }

        this.gui().open(player);
    }
}
