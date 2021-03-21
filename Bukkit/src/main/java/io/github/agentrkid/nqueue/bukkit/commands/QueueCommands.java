package io.github.agentrkid.nqueue.bukkit.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.jedis.ChainableMap;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageUtil;
import io.github.agentrkid.nqueue.api.object.QueueActions;
import io.github.agentrkid.nqueue.bukkit.nQueueBukkit;
import io.github.agentrkid.nqueue.bukkit.queue.QueueManager;
import io.github.agentrkid.nqueue.bukkit.utils.CC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommands {
    private final static QueueManager queueManager = nQueueBukkit.getInstance().getQueueManager();

    private final static String LINE = ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + StringUtils.repeat("-", 53);

    @Command(name = "", desc = "Displays the queue help message")
    @Require("queue.staff")
    public void displayHelpMessage(@Sender CommandSender sender) {

    }

    @Command(name = "join", desc = "Joins a queue")
    @Require("queue.staff")
    public void onJoinQueue(@Sender Player player, Queue queue) {
        JedisMessageUtil.sendMessage(QueueActions.PLAYER_ADD,
                ChainableMap.create().append("queueId", queue.getId()).append("playerId", player.getUniqueId().toString())
                        .append("playerName", player.getName()).append("playerPriority", 0),
                nQueueBukkit.getInstance().getJedisMessageHandler(), "Queue-independent");
    }

    @Command(name = "pause", desc = "Changes the pause state on a queue")
    @Require("queue.admin")
    public void pauseQueue(@Sender Player player, Queue queue) {
        JedisMessageUtil.sendMessage(QueueActions.QUEUE_STATE, ChainableMap.create().append("queueId", queue.getId())
                .append("state", !queue.isPaused()), nQueueBukkit.getInstance().getJedisMessageHandler(), "Queue-global");
        player.sendMessage(CC.translate("&aSent cross server request to all queues."));
    }

    @Command(name = "list", desc = "Displays the queues")
    @Require("queue.developer")
    public void displayQueueList(@Sender CommandSender sender) {
        sender.sendMessage(LINE);
        if (queueManager.getQueues().size() == 0) {
            sender.sendMessage(CC.translate("&cThere are no queues available."));
        } else {
            for (Queue queue : queueManager.getQueues()) {
                sender.sendMessage(CC.translate("&7" + queue.getId() + " &f&l-> &7"
                        + queue.getPlayersInQueue().size() + " &f&l-> "
                        + (queue.isPaused() ? "&aPaused" : "&cUnPaused")
                        + " &f&l -> &7" + queue.getOfflineQueuedPlayers().size() + "/" + queue.getPlayersInQueue().size()));
            }
        }
        sender.sendMessage(LINE);
    }
}
