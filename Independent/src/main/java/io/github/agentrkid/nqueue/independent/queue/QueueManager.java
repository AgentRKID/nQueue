package io.github.agentrkid.nqueue.independent.queue;

import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.QueuePlayer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class QueueManager {
    private final List<Queue> queues = new ArrayList<>();

    public void registerQueue(Queue queue) {
        this.queues.add(queue);
    }

    public Queue getQueueById(String id) {
        for (Queue queue : queues) {
            if (queue.getId().equalsIgnoreCase(queue.getId())) {
                return queue;
            }
        }
        return null;
    }

    public Queue getQueueByPlayer(QueuePlayer queuePlayer) {
        for (Queue queue : queues) {
            if (queue.getQueuePlayer(queuePlayer.getPlayerId()) != null) {
                return queue;
            }
        }
        return null;
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
