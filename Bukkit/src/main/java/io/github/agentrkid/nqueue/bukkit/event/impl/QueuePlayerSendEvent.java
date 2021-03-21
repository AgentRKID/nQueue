package io.github.agentrkid.nqueue.bukkit.event.impl;

import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.bukkit.event.QueueBaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@RequiredArgsConstructor
@Getter
public class QueuePlayerSendEvent extends QueueBaseEvent implements Cancellable {
    private final Player player;
    private final Queue queue;

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
