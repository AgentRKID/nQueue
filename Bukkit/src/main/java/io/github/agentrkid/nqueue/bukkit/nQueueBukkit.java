package io.github.agentrkid.nqueue.bukkit;

import com.jonahseguin.drink.CommandService;
import com.jonahseguin.drink.Drink;
import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.bukkit.commands.LeaveQueueCommand;
import io.github.agentrkid.nqueue.bukkit.commands.QueueCommands;
import io.github.agentrkid.nqueue.bukkit.commands.param.QueueProvider;
import io.github.agentrkid.nqueue.bukkit.jedis.QueueActionSubscriber;
import io.github.agentrkid.nqueue.bukkit.listener.ServerConnectionListener;
import io.github.agentrkid.nqueue.bukkit.queue.QueueManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class nQueueBukkit extends JavaPlugin {
    @Getter private static nQueueBukkit instance;

    private JedisMessageHandler jedisMessageHandler;

    private QueueManager queueManager;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        jedisMessageHandler = new JedisMessageHandler();

        queueManager = new QueueManager();

        new QueueActionSubscriber(jedisMessageHandler);

        Bukkit.getPluginManager().registerEvents(new ServerConnectionListener(), this);

        CommandService drink = Drink.get(this);

        drink.bind(Queue.class).toProvider(new QueueProvider());
        drink.register(new QueueCommands(), "queue", "q");
        drink.register(new LeaveQueueCommand(), "leavequeue");
        drink.registerCommands();
    }
}
