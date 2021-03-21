package io.github.agentrkid.nqueue.api.jedis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class JedisSub extends JedisPubSub {
    private static final JsonParser parser = new JsonParser();

    private final JedisMessageHandler handler;
    private final Jedis jedis;

    public JedisSub(JedisMessageHandler handler, String... channels) {
        this.handler = handler;

        this.jedis = new Jedis();

        new Thread(() -> {
            jedis.subscribe(this, channels);
        }).start();
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            JsonObject object = parser.parse(message).getAsJsonObject();
            String payload = object.get("payload").getAsString();
            JsonObject data = object.get("data").getAsJsonObject();
            this.onMessage(payload, data);
        } catch (JsonParseException ex) {
            ex.printStackTrace();
        }
    }

    public abstract void onMessage(String payload, JsonObject data);
}
