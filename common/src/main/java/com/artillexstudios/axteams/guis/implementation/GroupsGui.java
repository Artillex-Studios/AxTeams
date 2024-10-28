package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GroupsGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("groups.yml").toFile());

    public GroupsGui(User user) {
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
            LogUtils.warn("Attempted to open groups gui for offline player {} ({})", offlinePlayer.getName(), offlinePlayer.getUniqueId());
            return;
        }

        this.gui().open(player);
    }

    public ItemStack getItem(Group group, User member) {
        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(Group.class, group).add(User.class, member);
        Section section = this.config().getSection("group");
        if (section == null) {
            LogUtils.warn("No user section present for groups gui! Please reset, or fix your configuration!");
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

        for (Group value : team.values(TeamValues.GROUPS)) {
            this.gui().addItem(new GuiItem(this.getItem(value, this.user()), event -> {
                new GroupEditGui(this.user(), value).open();
            }));
        }
    }
}
