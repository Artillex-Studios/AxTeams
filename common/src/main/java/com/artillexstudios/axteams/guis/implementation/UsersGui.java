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
import com.artillexstudios.axteams.api.events.PreTeamInviteEvent;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Language;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.users.Users;
import com.artillexstudios.axteams.utils.AnvilInputUtils;
import com.artillexstudios.axteams.utils.FileUtils;
import com.artillexstudios.axteams.utils.IdentifiableSupplier;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class UsersGui extends GuiBase {
    private static final Config config = new Config(FileUtils.PLUGIN_DIRECTORY.resolve("guis/").resolve("users.yml").toFile());

    public UsersGui(User user) {
        super(user, config, true);
        ((com.artillexstudios.axteams.users.User) this.user()).guis().offer(new IdentifiableSupplier<>(UsersGui.class) {
            @Override
            public GuiBase get() {
                return new UsersGui(user);
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
                UUID uuid = event.getWhoClicked().getUniqueId();
                if (clickCooldown.hasCooldown(uuid)) {
                    return;
                }

                clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.guiActionCooldown);
                new UserGui(this.user(), member).open();
            }));
        }

        Context.Builder ctx = Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, this.user());
        Section section = this.config().getSection("invite");
        if (section == null) {
            LogUtils.warn("No invite section present for users gui! Please reset, or fix your configuration!");
            return;
        }

        ItemStack stack = new ItemBuilder(section, Placeholders.asMap(ctx)).get();
        this.gui().setItem(this.slots(this.config().get("invite.slots")), new GuiItem(stack, event -> {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (clickCooldown.hasCooldown(uuid)) {
                return;
            }

            clickCooldown.addCooldown(uuid, com.artillexstudios.axteams.config.Config.guiActionCooldown);
            if (!this.user().hasPermission(Permissions.INVITE)) {
                MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, Language.error.noPermission);
                return;
            }

            AnvilInput anvilInput = new AnvilInput.Builder()
                    .item(WrappedItemStack.wrap(new ItemStack(Material.PAPER)))
                    .title(StringUtils.format(this.config().getString("invite.slots")))
                    .event((inventoryClickEvent) -> {
                        inventoryClickEvent.setCancelled(true);

                        String input = AnvilInputUtils.getRenameText(inventoryClickEvent);
                        if (input.isBlank()) {
                            new UsersGui(this.user()).open();
                            return;
                        }

                        Player player = Bukkit.getPlayer(input);
                        if (player == null) {
                            MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, "Not online!");
                            return;
                        }

                        User invited = Users.getUserIfLoadedImmediately(player.getUniqueId());
                        if (invited == null) {
                            MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, "Not yet loaded!");
                            return;
                        }

                        if (team.members(true).contains(invited)) {
                            MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, "already member");
                            return;
                        }

                        if (team.invited(invited)) {
                            MessageUtils.sendMessage(this.user().onlinePlayer(), Language.prefix, "already invited");
                            return;
                        }

                        if (!new PreTeamInviteEvent(team, this.user(), invited).call()) {
                            return;
                        }

                        team.invite(invited);
                        new UsersGui(this.user()).open();
                    })
                    .closeEvent(closeEvent -> {
                        new UsersGui(this.user()).open();
                    })
                    .build((Player) event.getWhoClicked());

            anvilInput.open();
        }));
    }
}
