package com.artillexstudios.axteams.guis.actions;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.guis.GuiBase;
import com.artillexstudios.axteams.guis.actions.implementation.ActionConsoleCommand;
import com.artillexstudios.axteams.guis.actions.implementation.ActionOpen;
import com.artillexstudios.axteams.guis.actions.implementation.ActionPage;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.guis.actions.implementation.ActionPlayerCommand;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class Actions {
    private static final HashMap<String, Action<?>> ACTIONS = new HashMap<>();
    private static final Action<?> PAGE = register(new ActionPage());
    private static final Action<?> OPEN = register(new ActionOpen());
    private static final Action<?> PLAYER_COMMAND = register(new ActionPlayerCommand());
    private static final Action<?> CONSOLE_COMMAND = register(new ActionConsoleCommand());

    public static Action<?> register(Action<?> action) {
        ACTIONS.put(action.id(), action);
        return action;
    }

    public static void execute(User user, GuiBase guiBase, List<Action<?>> actions, List<Object> parsed) {
        for (int i = 0; i < actions.size(); i++) {
            Action<Object> action = (Action<Object>) actions.get(i);
            Object value = parsed.get(i);
            if (Config.DEBUG) {
                LogUtils.debug("Executing actions: {} with parameters: {}", action, parsed);
            }

            action.execute(user, guiBase, value);
        }
    }

    public static List<Action<?>> compile(List<String> actions) {
        ObjectArrayList<Action<?>> compiled = new ObjectArrayList<>();
        if (actions == null) {
            return compiled;
        }

        for (String rawAction : actions) {
            if (rawAction == null || rawAction.isBlank()) {
                continue;
            }

            String id = StringUtils.substringBetween(rawAction, "[", "]").toLowerCase(Locale.ENGLISH);

            Action<?> action = ACTIONS.get(id);
            if (action == null) {
                continue;
            }

            compiled.add(action);
        }

        return compiled;
    }

    public static List<Object> parseAll(List<String> actions) {
        ObjectArrayList<Object> parsed = new ObjectArrayList<>();
        if (actions == null) {
            return parsed;
        }

        for (String rawAction : actions) {
            if (rawAction == null || rawAction.isBlank()) {
                continue;
            }

            String id = StringUtils.substringBetween(rawAction, "[", "]").toLowerCase(Locale.ENGLISH);
            String arguments = StringUtils.substringAfter(rawAction, "] ");

            Action<?> action = ACTIONS.get(id);
            if (action == null) {
                LogUtils.warn("Found incorrect action: {}! Possible values are: {}", id, String.join(", ", ACTIONS.keySet()));
                continue;
            }

            parsed.add(action.evaluate(arguments));
        }

        return parsed;
    }
}
