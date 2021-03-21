package io.github.agentrkid.nqueue.independent.threads;

import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.QueuePlayer;
import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.logger.Logger;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.independent.Application;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class QueueDecayThread extends Thread {
    private final JedisMessageHandler handler;

    @Override
    public void run() {
        while (true) {
            // Make a copy of the queues
            List<Queue> queues = Application.getInstance().getQueueManager().getQueues();

            for (Queue queue : queues) {
                for (QueuePlayer offlinePlayer : queue.getOfflineQueuedPlayers()) {
                    if (offlinePlayer.shouldRemove()) {
                        JedisMessageUtil.sendMessage(QueueActions.OFFLINE_PLAYER_REMOVE,
                                ChainableMap.create().append("playerId", offlinePlayer.getPlayerId()), handler, "Queue-global");
                        Logger.log("Decay has removed " + offlinePlayer.getUsername() + ".");
                    }
                }
            }

            try {
                Thread.sleep(250L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
