package io.github.agentrkid.nqueue.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class QueuePlayer {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final String username;
    private final UUID playerId;
    private int priority;
    private Queue queue;

    public QueuePlayer(final String username, final UUID playerId, final int priority) {
        this.username = username;
        this.playerId = playerId;
        this.priority = priority;
        this.queue = null;
    }

    private boolean online = true;
    private long lastSeen;

    public boolean shouldRemove() {
        return !online && System.currentTimeMillis() > (lastSeen + (60 * 1000L * 5));
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
