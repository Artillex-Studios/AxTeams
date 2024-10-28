package com.artillexstudios.axteams.api.events;

import com.artillexstudios.axteams.api.teams.Team;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class TeamEvent extends Event {
    private final Team team;

    public TeamEvent(Team team) {
        this.team = team;
    }

    public Team team() {
        return this.team;
    }

    public boolean call() {
        if (this instanceof Cancellable cancellable) {
            return !cancellable.isCancelled();
        }

        return true;
    }
}
