package io.github.agentrkid.nqueue.independent;

import com.google.gson.*;
import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.api.jedis.JedisMessageHandler;
import io.github.agentrkid.nqueue.api.logger.Logger;
import io.github.agentrkid.nqueue.independent.jedis.QueueActionSubscriber;
import io.github.agentrkid.nqueue.independent.queue.QueueManager;
import io.github.agentrkid.nqueue.independent.threads.QueueBroadcastThread;
import io.github.agentrkid.nqueue.independent.threads.QueueDataUpdateThread;
import io.github.agentrkid.nqueue.independent.threads.QueueDecayThread;
import io.github.agentrkid.nqueue.independent.threads.QueueSendThread;
import lombok.Getter;

import java.io.*;

@Getter
public class Application {
    public static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    @Getter private static Application instance;

    private final File config;

    private final JedisMessageHandler jedisMessageHandler;

    private final QueueManager queueManager;


    public static void main(String[] args) {
        try {
            new Application();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.out.println("Get out of here!");
        }
    }

    public Application() throws IOException {
        instance = this;

        this.jedisMessageHandler = new JedisMessageHandler();

        this.queueManager = new QueueManager();

        config = new File("queue-config.json");
        if (!config.exists()) {
            if (config.createNewFile()) {
                JsonObject configObject = new JsonObject();

                JsonArray queueArray = new JsonArray();

                Queue queue = new Queue("Hub-1");

                JsonObject queueObject = new JsonObject();
                queueObject.addProperty("name", queue.getId());

                queueArray.add(queueObject);

                configObject.add("queues", queueArray);

                BufferedWriter writer = new BufferedWriter(new FileWriter(config));
                writer.write(GSON.toJson(configObject));
                writer.close();

                queueManager.registerQueue(queue);

                Logger.log("Created default config.");
            } else {
                Logger.log("Failed to create default config aborting");
                System.exit(0);
            }
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(config));
            JsonObject configObject = (new JsonParser()).parse(reader).getAsJsonObject();

            if (configObject.has("queues")) {
                JsonArray queueArray = configObject.getAsJsonArray("queues");

                for (JsonElement element : queueArray) {
                    Queue queue = new Queue(element.getAsJsonObject().get("name").getAsString());
                    queueManager.registerQueue(queue);

                    Logger.log("Created & registered the " + queue.getId() + " queue.");
                }
            } else {
                Logger.log("No queue array found, please delete the config file and restart!");
                System.exit(0);
            }
        }

        new QueueActionSubscriber(jedisMessageHandler);

        (new QueueBroadcastThread(jedisMessageHandler)).start();
        (new QueueDataUpdateThread(jedisMessageHandler)).start();
        (new QueueDecayThread(jedisMessageHandler)).start();
        (new QueueSendThread(jedisMessageHandler)).start();
    }
}
