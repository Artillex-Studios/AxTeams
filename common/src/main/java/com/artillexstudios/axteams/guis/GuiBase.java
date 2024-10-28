package com.artillexstudios.axteams.guis;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.placeholders.Context;
import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axapi.placeholders.ResolutionType;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.NumberUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.actions.Action;
import com.artillexstudios.axteams.guis.actions.Actions;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Map;

public abstract class GuiBase {
    private final User user;
    private final Config config;
    private final BaseGui gui;

    public GuiBase(User user, Config config, boolean paginated) {
        this.user = user;
        this.config = config;
        if (paginated) {
            this.gui = Gui.paginated()
                    .title(StringUtils.format(Placeholders.parse(config.getString("title"), Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, user))))
                    .pageSize(config.getInt("page-size"))
                    .rows(config.getInt("rows"))
                    .disableAllInteractions()
                    .create();
        } else {
            this.gui = Gui.gui()
                    .title(StringUtils.format(Placeholders.parse(config.getString("title"), Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, user))))
                    .rows(config.getInt("rows"))
                    .disableAllInteractions()
                    .create();
        }

        this.populate();
    }

    public void populate() {
        for (Map<Object, Object> item : this.config.getMapList("items")) {
            Object slotConfig = item.get("slots");
            if (slotConfig == null) {
                LogUtils.warn("Could not load item {} as it does not have any slots set!", item);
                continue;
            }

            IntList slots = this.slots(slotConfig);
            if (com.artillexstudios.axteams.config.Config.DEBUG) {
                LogUtils.debug("Populating gui with item: {}. Slots: {}", item, slots);
            }

            List<String> actionSet = (List<String>) item.get("actions");
            List<Action<?>> actions = Actions.compile(actionSet);
            List<Object> values = Actions.parseAll(actionSet);
            Map<String, String> placeholders = Placeholders.asMap(Context.builder(ParseContext.INTERNAL, ResolutionType.OFFLINE).add(User.class, this.user));
            if (com.artillexstudios.axteams.config.Config.DEBUG) {
                LogUtils.debug("Placeholders for item: {}", placeholders);
            }

            this.gui.setItem(slots, new GuiItem(new ItemBuilder(item, placeholders).get(), event -> {
                Actions.execute(this.user, this, actions, values);
            }));
        }
    }

    public IntArrayList slots(Object slots) {
        if (com.artillexstudios.axteams.config.Config.DEBUG) {
            LogUtils.debug("Getting slots for object: {}", slots);
        }

        if (slots instanceof Integer integer) {
            return new IntArrayList(List.of(integer));
        } else if (slots instanceof List<?> list) {
            IntArrayList integers = new IntArrayList();
            List<String> l = (List<String>) list;
            for (String str : l) {
                if (NumberUtils.isInt(str)) {
                    integers.add(Integer.parseInt(str));
                } else {
                    String[] split = str.split("-");
                    int min = Integer.parseInt(split[0]);
                    int max = Integer.parseInt(split[1]);
                    for (int i = min; i <= max; i++) {
                        integers.add(i);
                    }
                }
            }

            return integers;
        }

        return new IntArrayList();
    }

    public Config config() {
        return this.config;
    }

    public BaseGui gui() {
        return this.gui;
    }

    public abstract void open();

    public User user() {
        return this.user;
    }
}
