package com.wobble.wcombatlog.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class WCombatLogUntagEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String reason;

    public WCombatLogUntagEvent(Player player, String reason) {
        this.player = player;
        this.reason = reason;
    }

    public Player getPlayer() { return player; }
    public String getReason() { return reason; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
