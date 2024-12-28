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
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import com.artillexstudios.axteams.utils.IdentifiableSupplier;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class UserGroupEditGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("user-group-editor.yml").toFile());
    private final User other;

    public UserGroupEditGui(User user, User other) {
        super(user, config, true, true);
        this.other = other;
        ((com.artillexstudios.axteams.users.User) this.user()).guis().offer(new IdentifiableSupplier<>(UserGroupEditGui.class) {
            @Override
            public GuiBase get() {
                return new UserGroupEditGui(user, other);
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

    private ItemStack getItem(String item, Group group, User member) {
        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE)
                .add(Group.class, group)
                .add(User.class, member);
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

        for (Group value : team.values(TeamValues.GROUPS)) {
            if (value == this.other.group()) {
                this.gui().addItem(new GuiItem(this.getItem("current-group", value, this.user())));
            } else {
                this.gui().addItem(new GuiItem(this.getItem("group", value, this.user()), event -> {
                    UUID uuid = event.getWhoClicked().getUniqueId();
                    if (clickCooldown.hasCooldown(uuid)) {
                        return;
                    }

                    clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.guiActionCooldown);
                    if (!user().hasPermission(Permissions.USER_GROUP_MODIFY, other)) {
                        MessageUtils.sendMessage(user().onlinePlayer(), Language.prefix, Language.error.noPermission);
                        return;
                    }

                    other.group(value);
                    MessageUtils.sendMessage(user().onlinePlayer(), Language.prefix, Language.success.groupChangedForOther, Placeholder.unparsed("player", other.name()), Placeholder.component("group", value.displayName()));
                    if (this.other.onlinePlayer() != null) {
                        MessageUtils.sendMessage(this.other.onlinePlayer(), Language.prefix, Language.success.groupChanged, Placeholder.component("group", value.displayName()));
                    }
                    new UserGroupEditGui(this.user(), this.other).open(((PaginatedGui) this.gui()).getCurrentPageNum());
                }));
            }
        }
    }

    @Override
    public String title(String title) {
        return StringUtils.formatToString(Placeholders.parse(title, Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, this.other)));
    }
}
