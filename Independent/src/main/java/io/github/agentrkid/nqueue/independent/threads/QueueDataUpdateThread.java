package io.github.agentrkid.nqueue.independent.threads;

import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.independent.Application;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QueueDataUpdateThread extends Thread {
    private final JedisMessageHandler handler;

    @Override
    public void run() {
        while (true) {
            for (Queue queue : Application.getInstance().getQueueManager().getQueues()) {
                JedisMessageUtil.sendMessage(QueueActions.QUEUE_UPDATE_DATA,
                        ChainableMap.create().append("queueId", queue.getId())
                                .append("paused", queue.isPaused())
                                .append("online", queue.isOnline())
                                .append("playersInQueue", queue.getPlayersInQueue()),
                        handler, "Queue-bukkit");
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
