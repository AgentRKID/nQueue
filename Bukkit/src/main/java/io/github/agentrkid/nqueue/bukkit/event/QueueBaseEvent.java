package io.github.agentrkid.nqueue.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QueueBaseEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
