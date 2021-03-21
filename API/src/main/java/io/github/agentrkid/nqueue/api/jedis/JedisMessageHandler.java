package io.github.agentrkid.nqueue.api.jedis;

import com.google.gson.JsonObject;
import io.github.agentrkid.nqueue.api.logger.Logger;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SuppressWarnings("UnusedReturnValue")
@Getter
public class JedisMessageHandler {
    private final JedisPool pool;

    public JedisMessageHandler() {
        this.pool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);
        Logger.log("Successfully connected to redis.");
    }

    /**
     * Check if the Jedis Pool is connected.
     *
     * @return whether or not if the pool is connected.
     */
    public boolean isActive() {
        return !this.pool.isClosed();
    }

    /**
     * Close Jedis Pool.
     */
    public void close() {
        if (this.isActive()) {
            this.pool.close();
        }
    }

    /**
     * Write a json object to a Jedis pub sub.
     *
     * @param id - id of payload
     * @param data - data to be sent.
     * @param channel - channel to be sent to.
     */
    public void writeMessage(Enum<?> id, JsonObject data, final String channel) {
        final JsonObject object = new JsonObject();

        object.addProperty("payload", id.name());
        object.add("data", data == null ? new JsonObject() : data);

        try {
            if (!isActive()) {
                throw new Exception("Cannot publish a message with a closed or null pool.");
            }

            try {
                Jedis jedis = this.pool.getResource();
                jedis.publish(channel, object.toString());
                this.pool.returnResource(jedis);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
