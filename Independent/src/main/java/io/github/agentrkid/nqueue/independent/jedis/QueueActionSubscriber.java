package io.github.agentrkid.nqueue.independent.jedis;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.QueuePlayer;
import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.jedis.JedisSub;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.independent.Application;
import io.github.agentrkid.nqueue.independent.logger.Logger;

import java.util.UUID;

public class QueueActionSubscriber extends JedisSub {
    public QueueActionSubscriber(JedisMessageHandler handler) {
        super(handler, "Queue-independent", "Queue-global");
    }

    @Override
    public void onMessage(String payload, JsonObject data) {
        QueueActions action = QueueActions.valueOf(payload);

        switch(action) {
            case PLAYER_JOIN_PROXY: {
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());
                QueuePlayer queuePlayer = Application.getInstance().getQueueManager().getQueuePlayer(playerId);

                Logger.log(playerId.toString() + ", " + queuePlayer.getUsername());
                Logger.log("Joined proxy.");

                if (queuePlayer != null && !queuePlayer.isOnline()) {
                    queuePlayer.setOnline(true);
                    queuePlayer.setLastSeen(-1L);

                    Logger.log(queuePlayer.getUsername() + " has joined the network.");
                }
                break;
            }

            case PLAYER_LEAVE_PROXY: {
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());
                QueuePlayer queuePlayer = Application.getInstance().getQueueManager().getQueuePlayer(playerId);

                Logger.log(playerId.toString() + ", " + queuePlayer.getUsername());
                Logger.log("Left proxy.");

                if (queuePlayer != null && queuePlayer.isOnline()) {
                    queuePlayer.setOnline(false);
                    queuePlayer.setLastSeen(System.currentTimeMillis());

                    Logger.log(queuePlayer.getUsername() + " has left the network.");
                }
                break;
            }

            case PLAYER_ADD: {
                Queue queue = Application.getInstance().getQueueManager().getQueueById(data.get("queueId").getAsString());
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());

                QueuePlayer queuePlayer = new QueuePlayer(data.get("playerName").getAsString(), playerId, data.get("playerPriority").getAsInt());
                queue.getPlayersInQueue().add(queuePlayer);

                JedisMessageUtil.sendMessage(QueueActions.CONFIRM_PLAYER_ADD,
                        ChainableMap.create().append("queueId", queue.getId()).append("playerId", playerId.toString())
                                .append("playerName", queuePlayer.getUsername()).append("playerPriority", queuePlayer.getPriority()),
                        Application.getInstance().getJedisMessageHandler(), "Queue-bukkit");
                Logger.log("Added " + queuePlayer.getUsername() + " to the " + queue.getId() + " queue.");
                break;
            }

            case PLAYER_REMOVE: {
                Queue queue = Application.getInstance().getQueueManager().getQueueById(data.get("queueId").getAsString());
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());

                QueuePlayer queuePlayer = queue.getQueuePlayer(playerId);
                queue.getPlayersInQueue().remove(queuePlayer);

                JedisMessageUtil.sendMessage(QueueActions.CONFIRM_PLAYER_REMOVE, ChainableMap.create().append("queueId", queue.getId())
                                .append("playerId", playerId.toString()),
                        Application.getInstance().getJedisMessageHandler(), "Queue-bukkit");
                Logger.log("Removed " + queuePlayer.getUsername() + " from the " + queue.getId() + " queue.");
                break;
            }

            case OFFLINE_PLAYER_REMOVE: {
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());
                QueuePlayer queuePlayer = Application.getInstance().getQueueManager().getQueuePlayer(playerId);

                if (queuePlayer != null) {
                    Queue queue = Application.getInstance().getQueueManager().getQueueByPlayer(queuePlayer);
                    queue.getPlayersInQueue().remove(queuePlayer);
                }
                break;
            }

            case CONFIRM_PLAYER_SEND: {
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());
                QueuePlayer queuePlayer = Application.getInstance().getQueueManager().getQueuePlayer(playerId);
                Queue queue = Application.getInstance().getQueueManager().getQueueByPlayer(queuePlayer);

                queue.getPlayersInQueue().remove(queuePlayer);
                Logger.log("Sent " + queuePlayer.getUsername() + " to " + queue.getId());
                break;
            }

            case QUEUE_STATE: {
                Queue queue = Application.getInstance().getQueueManager().getQueueById(data.get("queueId").getAsString());

                Logger.log("Queue state changed.");

                if (queue != null) {
                    queue.setPaused(data.get("state").getAsBoolean());
                    Logger.log("Received a state change for " + queue.getId() + " (" + queue.isPaused() + ")");
                }
            }
        }
    }
}
