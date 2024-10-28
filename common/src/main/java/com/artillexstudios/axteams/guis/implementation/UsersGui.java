package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class UsersGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("users.yml").toFile());

    public UsersGui(User user) {
        super(user, config, true);
    }

    @Override
    public void open() {
        OfflinePlayer offlinePlayer = this.user().player();
        if (com.artillexstudios.axteams.config.Config.DEBUG) {
            LogUtils.debug("Open called for user: {}", offlinePlayer.getName());
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            LogUtils.warn("Attempted to open users gui for offline player {} ({})", offlinePlayer.getName(), offlinePlayer.getUniqueId());
            return;
        }

        this.gui().open(player);
    }

    public ItemStack getItem(User member) {
        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, member);
        Section section = this.config().getSection("user");
        if (section == null) {
            LogUtils.warn("No user section present for users gui! Please reset, or fix your configuration!");
            return new ItemStack(Material.BARRIER);
        }

        return new ItemBuilder(section, Placeholders.asMap(ctx)).get();
    }

    @Override
    public void populate() {
        super.populate();
        Team team = this.user().team();
        if (team == null) {
            return;
        }

        for (User member : team.members(true)) {
            this.gui().addItem(new GuiItem(this.getItem(member), event -> {
                System.out.println("wowowow");
            }));
        }
    }
}
