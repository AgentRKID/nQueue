package io.github.agentrkid.nqueue.independent.threads;

import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QueueBroadcastThread extends Thread {
    private final JedisMessageHandler handler;

    @Override
    public void run() {
        while (true) {
            JedisMessageUtil.sendMessage(QueueActions.QUEUE_BROADCAST, ChainableMap.create(), handler, "Queue-bukkit");

            try {
                Thread.sleep(25 * 1000L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
