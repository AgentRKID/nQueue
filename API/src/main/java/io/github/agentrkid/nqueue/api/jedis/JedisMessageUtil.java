package io.github.agentrkid.nqueue.api.jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Map;

public class JedisMessageUtil {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public static void sendMessage(Enum<?> update, ChainableMap data, JedisMessageHandler handler, String channel) {
        new Thread(() -> {
            JsonObject object = new JsonObject();
            for (Map.Entry<String, Object> entry : data.getMap().entrySet()) {
                object.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
            handler.writeMessage(update, object, channel);
        }).start();
    }
}
