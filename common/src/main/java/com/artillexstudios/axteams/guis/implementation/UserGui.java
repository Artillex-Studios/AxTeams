package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import com.artillexstudios.axteams.utils.IdentifiableSupplier;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class UserGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("user.yml").toFile());
    private final User other;

    public UserGui(User user, User other) {
        super(user, config, false, true);
        this.other = other;
        ((com.artillexstudios.axteams.users.User) this.user()).guis().offer(new IdentifiableSupplier<>(UserGui.class) {
            @Override
            public GuiBase get() {
                return new UserGui(user, other);
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

        this.populate();
        this.gui().open(player);
    }

    @Override
    public void populate() {
        super.populate();

        this.gui().setItem(this.slots(this.config().get("group.slots")), new GuiItem(this.getItem("group", this.user()), event -> {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.guiActionCooldown);
            if (!user().hasPermission(Permissions.USER_GROUP_MODIFY, this.other)) {
                MessageUtils.sendMessage(user().onlinePlayer(), Language.prefix, Language.error.noPermission);
                return;
            }

            new UserGroupEditGui(this.user(), this.other).open();
        }));

        this.gui().setItem(this.slots(this.config().get("user.slots")), new GuiItem(this.getItem("user", this.other)));
    }

    private ItemStack getItem(String item, User member) {
        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, member);
        Section section = this.config().getSection(item);
        if (section == null) {
            LogUtils.warn("No {} section present for groups gui! Please reset, or fix your configuration!", item);
            return new ItemStack(Material.BARRIER);
        }

        return new ItemBuilder(section, Placeholders.asMap(ctx)).get();
    }
}
