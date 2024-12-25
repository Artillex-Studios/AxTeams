package com.artillexstudios.axteams.api.events;

import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.users.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called before any checks are done to check if the player can join the team.
 */
public final class PreTeamInviteEvent extends TeamEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final User inviter;
    private final User invited;
    private boolean cancelled = false;

    public PreTeamInviteEvent(Team team, User inviter, User invited) {
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

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
