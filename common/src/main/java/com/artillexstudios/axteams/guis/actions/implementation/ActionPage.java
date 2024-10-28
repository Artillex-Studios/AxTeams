package com.artillexstudios.axteams.guis.actions.implementation;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.NumberUtils;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.actions.Action;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ActionPage extends Action<ActionPage.Direction> {

    public ActionPage() {
        super("page");
    }

    @Override
    public ActionPage.Direction evaluate(String input) {
        for (Direction value : Direction.values()) {
            if (ArrayUtils.contains(value.aliases(), input)) {
                return value;
            }
        }

        if (NumberUtils.isInt(input)) {
            return new Direction(Integer.parseInt(input));
        }

        LogUtils.warn("Invalid direction for PAGE action! Valid inputs: next, prev, [page number]! Found: {}", input);
        return Direction.FORWARD;
    }

    @Override
    public void execute(User user, GuiBase base, ActionPage.Direction value) {
        if (!(base.gui() instanceof PaginatedGui gui)) {
            return;
        }

        if (value.page() == -1) {
            gui.previous();
        } else if (value.page() == -2) {
            gui.next();
        } else {
            gui.setPageNum(value.page());
        }
    }

    public record Direction(int page, String... aliases) {
        private static final List<Direction> directions = new ArrayList<>();
        public static final Direction BACK = register(new Direction(-1, "backwards", "back", "prev", "previous"));
        public static final Direction FORWARD = register(new Direction(-2,"forwards", "forward", "next"));

        public static Direction register(Direction direction) {
            directions.add(direction);
            return direction;
        }

        public static List<Direction> values() {
            return Collections.unmodifiableList(directions);
        }
    }
}
