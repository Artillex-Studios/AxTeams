package com.artillexstudios.axteams.api.events;

import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class TeamInviteEvent extends TeamEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final User inviter;
    private final User invited;

    public TeamInviteEvent(Team team, User inviter, User invited) {
        super(team);
        this.inviter = inviter;
        this.invited = invited;
    }

    public User invited() {
        return this.invited;
    }

    public User inviter() {
        return this.inviter;
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
