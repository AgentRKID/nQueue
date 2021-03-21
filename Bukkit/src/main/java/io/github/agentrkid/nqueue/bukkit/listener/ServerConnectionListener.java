package io.github.agentrkid.nqueue.bukkit.listener;

import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.bukkit.nQueueBukkit;
import io.github.agentrkid.nqueue.bukkit.utils.CC;
import io.github.agentrkid.rabbit.bukkit.RabbitBukkit;
import io.github.agentrkid.rabbit.bukkit.events.impl.NetworkLeaveEvent;
import io.github.agentrkid.rabbit.bukkit.events.impl.ServerConnectedByProxyEvent;
import io.github.agentrkid.rabbit.bukkit.utils.MetadataUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ServerConnectionListener implements Listener {
    // We listen to this event no matter what because
    // the plugin should be on every server.
    @EventHandler
    public void onServerConnected(ServerConnectedByProxyEvent event) {
        Player player = event.getPlayer();

        if (nQueueBukkit.getInstance().getQueueManager().getQueuePlayer(player) != null) {
            JedisMessageUtil.sendMessage(QueueActions.PLAYER_JOIN_PROXY,
                    ChainableMap.create().append("playerId", player.getUniqueId().toString()), nQueueBukkit.getInstance().getJedisMessageHandler(),
                    "Queue-global");
            System.out.println("[nQueue] has took " + player.getUniqueId().toString() + " online.");
        }

        if (player.hasPermission("queue.staff")) {
            player.sendMessage(CC.translate("&7&oYour queue alerts have automatically been turned &a&oon&7."));
            MetadataUtil.addMetadata(player.getUniqueId(), "queue-alerts", true, false, null);
        }
    }

    @EventHandler
    public void onNetworkLeave(NetworkLeaveEvent event) {
        UUID playerId = event.getPlayerId();

        if (event.getFrom() == RabbitBukkit.getInstance().getCurrentServer()) {
            if (nQueueBukkit.getInstance().getQueueManager().getQueuePlayer(playerId) != null) {
                System.out.println("[nQueue] has took " + playerId + " offline.");
                JedisMessageUtil.sendMessage(QueueActions.PLAYER_LEAVE_PROXY, ChainableMap.create().append("playerId", playerId.toString()),
                        nQueueBukkit.getInstance().getJedisMessageHandler(), "Queue-global");
            }
        }
    }
}
