package io.github.agentrkid.nqueue.bukkit.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.bukkit.nQueueBukkit;
import io.github.agentrkid.nqueue.bukkit.utils.CC;
import org.bukkit.entity.Player;

public class LeaveQueueCommand {
    @Command(name = "", desc = "Leaves queue")
    public void leaveQueue(@Sender Player player) {
        if (nQueueBukkit.getInstance().getQueueManager().getQueuePlayer(player) == null) {
            player.sendMessage(CC.translate("&cYou are not queued."));
            return;
        }

        JedisMessageUtil.sendMessage(QueueActions.PLAYER_REMOVE,
                ChainableMap.create().append("queueId", nQueueBukkit.getInstance().getQueueManager().getQueueByPlayer(player).getId())
                        .append("playerId", player.getUniqueId().toString()),
                nQueueBukkit.getInstance().getJedisMessageHandler(), "Queue-independent");
    }
}
