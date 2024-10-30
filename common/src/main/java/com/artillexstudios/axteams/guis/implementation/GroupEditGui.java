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
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    private ItemStack getItem(String item, User member) {
        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(Group.class, group).add(User.class, member);
        Section section = this.config().getSection(item);
        if (section == null) {
            LogUtils.warn("No {} section present for groups gui! Please reset, or fix your configuration!", item);
            return new ItemStack(Material.BARRIER);
        }

        return new ItemBuilder(section, Placeholders.asMap(ctx)).get();
    }

    @Override
    public void populate() {
        super.populate();

        this.gui().setItem(this.slots(this.config().get("permissions.slots")), new GuiItem(this.getItem("permissions", this.user()), event -> {
//            new GroupEditGui(this.user(), value).open();
        }));

        this.gui().setItem(this.slots(this.config().get("priority.slots")), new GuiItem(this.getItem("priority", this.user()), event -> {
//            new GroupEditGui(this.user(), value).open();
        }));

        this.gui().setItem(this.slots(this.config().get("prefix.slots")), new GuiItem(this.getItem("prefix", this.user()), event -> {
//            new GroupEditGui(this.user(), value).open();
        }));

        this.gui().setItem(this.slots(this.config().get("rename.slots")), new GuiItem(this.getItem("rename", this.user()), event -> {
//            new GroupEditGui(this.user(), value).open();
        }));

        this.gui().setItem(this.slots(this.config().get("delete.slots")), new GuiItem(this.getItem("delete", this.user()), event -> {
//            new GroupEditGui(this.user(), value).open();
        }));
    }
}
