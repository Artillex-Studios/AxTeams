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
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import com.artillexstudios.axteams.utils.IdentifiableSupplier;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class PermissionEditGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("permission-editor.yml").toFile());
    private final Group group;

    public PermissionEditGui(User user, Group group) {
        super(user, config, true, true);
        this.group = group;

        ((com.artillexstudios.axteams.users.User) this.user()).guis().offer(new IdentifiableSupplier<>(PermissionEditGui.class) {
            @Override
            public GuiBase get() {
                Team team = user.team();
                if (team == null) {
                    return null;
                }

                if (!team.values(TeamValues.GROUPS).contains(group)) {
                    return null;
                }

                return new PermissionEditGui(user, group);
            }
        });
    }

    @Override
    public void open() {
        this.open(1);
    }

    @Override
    public void open(int page) {
        if (com.artillexstudios.axteams.config.Config.DEBUG) {
            LogUtils.debug("Open called for user: {}", this.user().name());
        }

        Player player = this.user().onlinePlayer();
        if (player == null) {
            LogUtils.warn("Attempted to open group editor gui for offline player {} ({})", this.user().name(), this.user().player().getUniqueId());
            return;
        }

        this.populate();
        if (this.gui() instanceof PaginatedGui paginatedGui) {
            paginatedGui.open(player, page);
        }
    }

    private ItemStack getItem(String item, Permission permission) {
        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(Group.class, group).add(User.class, this.user()).add(Permission.class, permission);
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
        Team team = this.user().team();
        if (team == null) {
            return;
        }

        for (Permission value : Permissions.values()) {
            if (this.group.permissions().contains(value)) {
                this.gui().addItem(new GuiItem(this.getItem("enabled", value), event -> {
                    UUID uuid = event.getWhoClicked().getUniqueId();
                    if (clickCooldown.hasCooldown(uuid)) {
                        return;
                    }

                    clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.GUI_ACTION_COOLDOWN);

                    this.group.permissions().remove(value);
                    team.markUnsaved();
                    new PermissionEditGui(this.user(), group).open(((PaginatedGui) this.gui()).getCurrentPageNum());
                }));
            } else {
                this.gui().addItem(new GuiItem(this.getItem("disabled", value), event -> {
                    UUID uuid = event.getWhoClicked().getUniqueId();
                    if (clickCooldown.hasCooldown(uuid)) {
                        return;
                    }

                    clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.GUI_ACTION_COOLDOWN);

                    this.group.permissions().add(value);
                    team.markUnsaved();
                    new PermissionEditGui(this.user(), group).open(((PaginatedGui) this.gui()).getCurrentPageNum());
                }));
            }
        }
    }
}
