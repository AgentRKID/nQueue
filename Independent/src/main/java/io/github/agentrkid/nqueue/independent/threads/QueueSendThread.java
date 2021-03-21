package io.github.agentrkid.nqueue.independent.threads;

import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.QueuePlayer;
import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.independent.Application;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class QueueSendThread extends Thread {
    private final JedisMessageHandler handler;

    @Override
    public void run() {
        while (true) {
            // Make a copy of the queues...
            List<Queue> queues = Application.getInstance().getQueueManager().getQueues();;

            for (Queue queue : queues) {
                if (!queue.isPaused() && !queue.getPlayersInQueue().isEmpty()) {
                    List<QueuePlayer> offlineQueuedPlayers = queue.getOfflineQueuedPlayers();
                    if (!offlineQueuedPlayers.isEmpty()) {
                        queue.getOfflineQueuedPlayers().forEach(queuePlayer -> {
                            JedisMessageUtil.sendMessage(QueueActions.OFFLINE_PLAYER_REMOVE, ChainableMap.create()
                                    .append("queueId", queue.getId())
                                    .append("queuePlayer", queuePlayer.toJson()),
                                    handler, "Queue-global");
                        });
                    }

                    // Maybe we just emptied the last people in the queue
                    // so who we gonna send?
                    if (queue.getPlayersInQueue().isEmpty()) {
                        return;
                    }

                    // Get the first person in the queue
                    QueuePlayer sendingPlayer = queue.getPlayersInQueue().get(0);

                    JedisMessageUtil.sendMessage(QueueActions.PLAYER_SEND, ChainableMap.create()
                            .append("playerId", sendingPlayer.getPlayerId()), handler, "Queue-bukkit");
                }
            }

            try {
                Thread.sleep(500L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
