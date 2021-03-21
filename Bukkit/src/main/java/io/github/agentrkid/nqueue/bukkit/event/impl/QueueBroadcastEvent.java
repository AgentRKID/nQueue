package io.github.agentrkid.nqueue.bukkit.event.impl;

import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.bukkit.event.QueueBaseEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@RequiredArgsConstructor
@Getter @Setter
public class QueueBroadcastEvent extends QueueBaseEvent implements Cancellable {
    private final Player player;
    private final Queue queue;

    private String[] queueBroadcastMessage;

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
