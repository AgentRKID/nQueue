package io.github.agentrkid.nqueue.bukkit.queue;

import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.QueuePlayer;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class QueueManager {
    private List<Queue> queues = new ArrayList<>();

    public Queue getQueueById(String name) {
        return queues.stream().filter(queue -> queue.getId().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public Queue getQueueByPlayer(Player player) {
        QueuePlayer queuePlayer = getQueuePlayer(player);
        if (queuePlayer == null) {
            return null;
        }
        return getQueueByQueuePlayer(queuePlayer);
    }

    public Queue getQueueByQueuePlayer(QueuePlayer queuePlayer) {
        for (Queue queue : queues) {
            if (queue.getQueuePlayer(queuePlayer.getPlayerId()) != null) {
                return queue;
            }
        }
        return null;
    }

    public QueuePlayer getQueuePlayer(Player player) {
        return getQueuePlayer(player.getUniqueId());
    }

    public QueuePlayer getQueuePlayer(UUID playerId) {
        for (Queue queue : queues) {
            QueuePlayer queuePlayer = queue.getQueuePlayer(playerId);

            if (queuePlayer != null) {
                return queuePlayer;
            }
        }
        return null;
    }
}
