package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.gui.AnvilInput;
import com.artillexstudios.axapi.items.WrappedItemStack;
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
import com.artillexstudios.axteams.utils.AnvilInputUtils;
import com.artillexstudios.axteams.utils.FileUtils;
import com.artillexstudios.axteams.utils.IdentifiableSupplier;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class GroupsGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("groups.yml").toFile());

    public GroupsGui(User user) {
        super(user, config, true);
        ((com.artillexstudios.axteams.users.User) this.user()).guis().offer(new IdentifiableSupplier<>(GroupsGui.class) {
            @Override
            public GuiBase get() {
                return new GroupsGui(user);
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
            LogUtils.warn("Attempted to open groups gui for offline player {} ({})", this.user().name(), this.user().player().getUniqueId());
            return;
        }

        this.gui().open(player);
    }

    private ItemStack getItem(Group group, User member) {
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

        List<Group> groups = team.values(TeamValues.GROUPS);
        groups.sort(Comparator.comparing(Group::priority));
        Collections.reverse(groups);
        for (int i = 0; i < groups.size(); i++) {
            Group value = groups.get(i);
            int finalI = i;
            this.gui().addItem(new GuiItem(this.getItem(value, this.user()), event -> {
                UUID uuid = event.getWhoClicked().getUniqueId();
                if (clickCooldown.hasCooldown(uuid)) {
                    return;
                }

                clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.guiActionCooldown);
                if (!event.isShiftClick() || !this.user().hasPermission(Permissions.GROUP_PRIORITY_CHANGE)) {
                    if (com.artillexstudios.axteams.config.Config.debug) {
                        LogUtils.debug("Opening group edit gui for group: {}", value.name());
                    }
                    new GroupEditGui(this.user(), value).open();
                    return;
                }

                if (event.isLeftClick()) {
                    if (com.artillexstudios.axteams.config.Config.debug) {
                        LogUtils.debug("Left click! I: {}, name: {}, priority: {}", finalI, value.name(), value.priority());
                    }
                    // Increase priority
                    if (value.priority() == Group.OWNER_PRIORITY || finalI == 0) {
                        // Already max priority
                        if (com.artillexstudios.axteams.config.Config.debug) {
                            LogUtils.debug("Already max priority!");
                        }
                        return;
                    }

                    Group other = groups.get(finalI - 1);
                    int otherPriority = other.priority();
                    if (com.artillexstudios.axteams.config.Config.debug) {
                        LogUtils.debug("Other: name: {}, priority: {}", other.name(), otherPriority);
                    }
                    other.priority(value.priority());
                    value.priority(otherPriority);
                } else {
                    if (com.artillexstudios.axteams.config.Config.debug) {
                        LogUtils.debug("Right click! I: {}, name: {}, priority: {}", finalI, value.name(), value.priority());
                    }
                    // Decrease priority
                    if (value.priority() == Group.DEFAULT_PRIORITY || finalI == groups.size() - 1) {
                        // Already min priority
                        return;
                    }

                    Group other = groups.get(finalI + 1);
                    int otherPriority = other.priority();
                    if (com.artillexstudios.axteams.config.Config.debug) {
                        LogUtils.debug("Other: name: {}, priority: {}", other.name(), otherPriority);
                    }
                    other.priority(value.priority());
                    value.priority(otherPriority);
                }

                new GroupsGui(this.user()).open();
            }));
        }

        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, this.user());
        Section section = this.config().getSection("create");
        if (section == null) {
            LogUtils.warn("No create section present for users gui! Please reset, or fix your configuration!");
            return;
        }

        ItemStack stack = new ItemBuilder(section, Placeholders.asMap(ctx)).get();
        this.gui().setItem(this.slots(this.config().get("create.slots")), new GuiItem(stack, event -> {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.guiActionCooldown);
            if (!this.user().hasPermission(Permissions.GROUP_CREATE)) {
                MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, Language.error.noPermission);
                return;
            }

            AnvilInput anvilInput = new AnvilInput.Builder()
                    .item(WrappedItemStack.wrap(new ItemStack(Material.PAPER)))
                    .title(StringUtils.format(this.config().getString("new-group-title")))
                    .event((inventoryClickEvent) -> {
                        inventoryClickEvent.setCancelled(true);

                        String input = AnvilInputUtils.getRenameText(inventoryClickEvent);
                        if (input.isBlank()) {
                            new GroupsGui(this.user()).open();
                            return;
                        }

                        for (Group value : team.values(TeamValues.GROUPS)) {
                            if (value.name().equalsIgnoreCase(input)) {
                                MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, Language.error.groupAlreadyExists, Placeholder.unparsed("group", input));
                                return;
                            }
                        }

                        for (Group value : team.values(TeamValues.GROUPS)) {
                            if (value.priority() == Group.DEFAULT_PRIORITY || value.priority() == Group.OWNER_PRIORITY) {
                                continue;
                            }

                            // Shift group priorities
                            value.priority(value.priority() + 1);
                        }
                        
                        Group group = new Group(0, input, Component.text(input), new ArrayList<>());
                        team.add(TeamValues.GROUPS, group);
                        MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, Language.success.groupCreated, Placeholder.unparsed("group", input));
                        new GroupEditGui(this.user(), group).open();
                    })
                    .closeEvent(closeEvent -> {
                        new GroupsGui(this.user()).open();
                    })
                    .build((Player) event.getWhoClicked());

            anvilInput.open();
        }));
    }
}
