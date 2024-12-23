package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.IdentifiableSupplier;
import org.bukkit.entity.Player;

public final class ConfigGui extends GuiBase {

    public ConfigGui(User user, Config config) {
        super(user, config, false);

        ((com.artillexstudios.axteams.users.User) this.user()).guis().offer(new IdentifiableSupplier<>(config) {
            @Override
            public GuiBase get() {
                Team team = user.team();
                if (team == null) {
                    return null;
                }

                return new ConfigGui(user, config);
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
            LogUtils.warn("Attempted to open {} gui for offline player {} ({})", this.config().getBackingDocument().getFile().getName(), this.user().name(), this.user().player().getUniqueId());
            return;
        }

        this.gui().open(player);
    }
}
