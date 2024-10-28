package com.artillexstudios.axteams.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreTeamCreateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PreTeamCreateEvent(String name) {

    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
