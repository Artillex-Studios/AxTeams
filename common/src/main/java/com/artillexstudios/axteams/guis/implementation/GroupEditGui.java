package com.artillexstudios.axteams.guis.implementation;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.gui.AnvilInput;
import com.artillexstudios.axapi.gui.SignInput;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.Version;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.utils.FileUtils;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("removal")
public final class GroupEditGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("group-editor.yml").toFile());
    private final Group group;

    public GroupEditGui(User user, Group group) {
        super(user, config, false, true);
        this.group = group;
    }

    @Override
    public void open() {
        if (com.artillexstudios.axteams.config.Config.DEBUG) {
            LogUtils.debug("Open called for user: {}", this.user().name());
        }

        Player player = this.user().onlinePlayer();
        if (player == null) {
            LogUtils.warn("Attempted to open group editor gui for offline player {} ({})", this.user().name(), this.user().player().getUniqueId());
            return;
        }

        this.populate();
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
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.GUI_ACTION_COOLDOWN);
            if (!user().hasPermission(Permissions.GROUP_PERMISSIONS_EDIT, this.group)) {
                MessageUtils.sendMessage(user().onlinePlayer(), Language.PREFIX, Language.NO_PERMISSION);
                return;
            }

            new PermissionEditGui(this.user(), this.group).open();
        }));

        this.gui().setItem(this.slots(this.config().get("priority.slots")), new GuiItem(this.getItem("priority", this.user()), event -> {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.GUI_ACTION_COOLDOWN);
            if (!user().hasPermission(Permissions.GROUP_PRIORITY_CHANGE, this.group)) {
                MessageUtils.sendMessage(user().onlinePlayer(), Language.PREFIX, Language.NO_PERMISSION);
                return;
            }

            SignInput signInput = new SignInput.Builder()
                    .setLines(List.of(Component.empty(), Component.text("Enter the new priority"), Component.empty(), Component.empty()))
                    .setHandler((player, lines) -> {
                        String firstLine = PlainTextComponentSerializer.plainText().serialize(lines[0]);
                        try {
                            int priority = Integer.parseInt(firstLine);
                            group.priority(priority);
                        } catch (NumberFormatException exception) {
                            MessageUtils.sendMessage(player, "<red>Failed to parse number!");
                            return;
                        }

                        // TODO: Change the priority of different groups, permission checks
                        new GroupEditGui(GroupEditGui.this.user(), GroupEditGui.this.group).open();
                    })
                    .build((Player) event.getWhoClicked());

            signInput.open();
        }));

        this.gui().setItem(this.slots(this.config().get("prefix.slots")), new GuiItem(this.getItem("prefix", this.user()), event -> {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.GUI_ACTION_COOLDOWN);
            if (!user().hasPermission(Permissions.GROUP_PREFIX_CHANGE, this.group)) {
                MessageUtils.sendMessage(user().onlinePlayer(), Language.PREFIX, Language.NO_PERMISSION);
                return;
            }

//            SignInput signInput = new SignInput.Builder()
//                    .setLines(List.of(Component.empty(), Component.text("Enter the new prefix"), Component.empty(), Component.empty()))
//                    .setHandler((player, lines) -> {
//                        String firstLine = PlainTextComponentSerializer.plainText().serialize(lines[0]);
//                        if (firstLine.isBlank()) {
//                            MessageUtils.sendMessage(player, "<red>You did not input a new name!");
//                            return;
//                        }
//
//                        group.displayName(lines[0]);
//                        new GroupEditGui(GroupEditGui.this.user(), GroupEditGui.this.group).open();
//                    })
//                    .build((Player) event.getWhoClicked());
//
//            signInput.open();
            AnvilInput anvilInput = new AnvilInput.Builder()
                    .item(WrappedItemStack.edit(new ItemStack(Material.PAPER), item -> {
                        item.set(DataComponents.customName(), this.group.displayName());
                        return item;
                    }))
                    .title(Component.text("Enter a new display name!"))
                    .event((inventoryClickEvent) -> {
                        inventoryClickEvent.setCancelled(true);

                        String text;
                        if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21)) {
                            text = ((AnvilView) inventoryClickEvent.getView()).getRenameText();
                        } else {
                            text = ((AnvilInventory) inventoryClickEvent.getInventory()).getRenameText();
                        }

                        this.group.displayName(StringUtils.format(text));
                        Team team = this.user().team();
                        if (team != null) {
                            team.markUnsaved();
                        }

                        user().onlinePlayer().closeInventory();
                    })
                    .closeEvent(closeEvent -> {
                        new GroupEditGui(GroupEditGui.this.user(), GroupEditGui.this.group).open();
                    })
                    .build((Player) event.getWhoClicked());

            anvilInput.open();
        }));

        // TODO: Proper lang from files
        this.gui().setItem(this.slots(this.config().get("rename.slots")), new GuiItem(this.getItem("rename", this.user()), event -> {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.GUI_ACTION_COOLDOWN);
            if (!user().hasPermission(Permissions.GROUP_RENAME, this.group)) {
                MessageUtils.sendMessage(user().onlinePlayer(), Language.PREFIX, Language.NO_PERMISSION);
                return;
            }

            SignInput signInput = new SignInput.Builder()
                    .setLines(List.of(Component.empty(), Component.text("Enter the new name"), Component.empty(), Component.empty()))
                    .setHandler((player, lines) -> {
                        String firstLine = PlainTextComponentSerializer.plainText().serialize(lines[0]);
                        if (firstLine.isBlank()) {
                            MessageUtils.sendMessage(player, "<red>You did not input a new name!");
                            return;
                        }

                        group.name(firstLine);
                        new GroupEditGui(GroupEditGui.this.user(), GroupEditGui.this.group).open();
                    })
                    .build((Player) event.getWhoClicked());

            signInput.open();
        }));

        this.gui().setItem(this.slots(this.config().get("delete.slots")), new GuiItem(this.getItem("delete", this.user()), event -> {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.GUI_ACTION_COOLDOWN);
            if (!user().hasPermission(Permissions.GROUP_DELETE, this.group)) {
                MessageUtils.sendMessage(user().onlinePlayer(), Language.PREFIX, Language.NO_PERMISSION);
                return;
            }

//            new GroupEditGui(this.user(), value).open();
        }));
    }
}
