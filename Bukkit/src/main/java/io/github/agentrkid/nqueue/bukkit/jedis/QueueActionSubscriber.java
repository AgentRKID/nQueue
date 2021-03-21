package io.github.agentrkid.nqueue.bukkit.jedis;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.QueuePlayer;
import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.jedis.JedisSub;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.bukkit.event.impl.QueueBroadcastEvent;
import io.github.agentrkid.nqueue.bukkit.event.impl.QueuePlayerSendEvent;
import io.github.agentrkid.nqueue.bukkit.nQueueBukkit;
import io.github.agentrkid.nqueue.bukkit.utils.CC;
import io.github.agentrkid.rabbit.api.RabbitServer;
import io.github.agentrkid.rabbit.shared.RabbitShared;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QueueActionSubscriber extends JedisSub {
    public QueueActionSubscriber(JedisMessageHandler handler) {
        super(handler, "Queue-bukkit", "Queue-global");
    }

    @Override
    public void onMessage(String payload, JsonObject data) {
        QueueActions queueAction = QueueActions.valueOf(payload);


        switch(queueAction) {
            case PLAYER_JOIN_PROXY: {
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());
                QueuePlayer queuePlayer = nQueueBukkit.getInstance().getQueueManager().getQueuePlayer(playerId);

                if (queuePlayer != null && !queuePlayer.isOnline()) {
                    queuePlayer.setOnline(true);
                    queuePlayer.setLastSeen(-1L);
                }
                break;
            }

            case PLAYER_LEAVE_PROXY: {
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());
                QueuePlayer queuePlayer = nQueueBukkit.getInstance().getQueueManager().getQueuePlayer(playerId);

                if (queuePlayer != null && !queuePlayer.isOnline()) {
                    queuePlayer.setOnline(false);
                    queuePlayer.setLastSeen(System.currentTimeMillis());
                }
                break;
            }

            case QUEUE_UPDATE_DATA: {
                String queueId = data.get("queueId").getAsString();

                Queue queue = nQueueBukkit.getInstance().getQueueManager().getQueueById(queueId);

                if (queue == null) {
                    queue = new Queue(queueId);
                    JsonArray players = data.getAsJsonArray("playersInQueue");

                    for (JsonElement element : players) {
                        JsonObject playerObject = element.getAsJsonObject();

                        QueuePlayer queuePlayer = new QueuePlayer(playerObject.get("username").getAsString(), UUID.fromString(playerObject.get("playerId").getAsString()), playerObject.get("priority").getAsInt());
                        queuePlayer.setOnline(playerObject.get("online").getAsBoolean());
                        queuePlayer.setLastSeen(playerObject.get("lastSeen").getAsLong());

                        queue.getPlayersInQueue().add(queuePlayer);
                    }

                    queue.setPaused(data.get("paused").getAsBoolean());
                    nQueueBukkit.getInstance().getQueueManager().getQueues().add(queue);
                }
                break;
            }

            case QUEUE_STATE: {
                Queue queue = nQueueBukkit.getInstance().getQueueManager().getQueueById(data.get("queueId").getAsString());

                if (queue != null) {
                    queue.setPaused(data.get("state").getAsBoolean());
                }
                break;
            }

            case PLAYER_SEND: {
                UUID playerId;

                try {
                    playerId = UUID.fromString(data.get("playerId").getAsString());
                } catch (Exception ignored) {
                    return;
                }

                Player player = Bukkit.getPlayer(playerId);

                if (player != null && player.isOnline()) {
                    Queue queue = nQueueBukkit.getInstance().getQueueManager().getQueueByPlayer(player);
                    RabbitServer connectingTo = RabbitShared.getInstance().getServerManager().getServerById(queue.getId());

                    if (connectingTo == null || connectingTo.isWhitelisted()
                            || !connectingTo.isOnline()) {
                        JedisMessageUtil.sendMessage(QueueActions.QUEUE_STATE, ChainableMap.create().append("queueId", queue.getId())
                                .append("state", !queue.isPaused()), nQueueBukkit.getInstance().getJedisMessageHandler(), "Queue-global");
                        return;
                    }

                    QueuePlayerSendEvent event = new QueuePlayerSendEvent(player, queue);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }

                    QueuePlayer queuePlayer = queue.getQueuePlayer(player.getUniqueId());

                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(queue.getId());
                    player.sendPluginMessage(nQueueBukkit.getInstance(), "BungeeCord", out.toByteArray());

                    JedisMessageUtil.sendMessage(QueueActions.CONFIRM_PLAYER_SEND, ChainableMap.create().append("playerId", player.getUniqueId().toString()),
                            nQueueBukkit.getInstance().getJedisMessageHandler(), "Queue-independent");

                    player.sendMessage(CC.translate("&7Sending you to " + queue.getId() + "."));
                    queue.getPlayersInQueue().remove(queuePlayer);
                }
                break;
            }

            case CONFIRM_PLAYER_ADD: {
                Queue queue = nQueueBukkit.getInstance().getQueueManager().getQueueById(data.get("queueId").getAsString());
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());

                QueuePlayer queuePlayer = new QueuePlayer(data.get("playerName").getAsString(), playerId, data.get("playerPriority").getAsInt());
                queue.getPlayersInQueue().add(queuePlayer);

                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(CC.translate("&aYou have been added to the " + queue.getId() + " queue!"));
                }
                break;
            }

            case CONFIRM_PLAYER_REMOVE: {
                Queue queue = nQueueBukkit.getInstance().getQueueManager().getQueueById(data.get("queueId").getAsString());
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());

                queue.getPlayersInQueue().remove(queue.getQueuePlayer(playerId));

                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(CC.translate("&aYou have left the " + queue.getId() + " queue."));
                }
                break;
            }

            case OFFLINE_PLAYER_REMOVE: {
                UUID playerId = UUID.fromString(data.get("playerId").getAsString());
                QueuePlayer queuePlayer = nQueueBukkit.getInstance().getQueueManager().getQueuePlayer(playerId);

                if (queuePlayer != null) {
                    Queue queue = nQueueBukkit.getInstance().getQueueManager().getQueueByQueuePlayer(queuePlayer);
                    queue.getPlayersInQueue().remove(queuePlayer);
                }
                break;
            }

            case QUEUE_BROADCAST: {
                for (Queue queue : nQueueBukkit.getInstance().getQueueManager().getQueues()) {
                    for (QueuePlayer queuePlayer : queue.getPlayersInQueue()) {
                        Player player = Bukkit.getPlayer(queuePlayer.getPlayerId());

                        if (player != null && player.isOnline()) {
                            QueueBroadcastEvent event = new QueueBroadcastEvent(player, queue);
                            event.setQueueBroadcastMessage(new String[] {
                                    "",
                                    CC.translate("&7You are position &f#" + queue.getPlayerPosition(queuePlayer) + " &7in the &f" + queue.getId() + " &7queue."),
                                    CC.translate("&7To leave the queue, use /leavequeue"),
                                    ""
                            });

                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                return;
                            }
                            player.sendMessage(event.getQueueBroadcastMessage());
                        }
                    }
                }
                break;
            }
        }
    }
}
