package io.github.agentrkid.nqueue.api;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class Queue {
    private String id;
    private boolean paused;
    private boolean online;
    private List<QueuePlayer> playersInQueue;

    public Queue(String id) {
        this.id = id;
        this.paused = false;
        this.online = true;
        this.playersInQueue = new ArrayList<>();
    }

    public int getPlayerPosition(QueuePlayer queuePlayer) {
        for (int i = 0; i < playersInQueue.size(); ++i) {
            if (playersInQueue.get(i).getPlayerId().equals(queuePlayer.getPlayerId())) {
                return i + 1;
            }
        }
        return -1;
    }

    public QueuePlayer getQueuePlayer(UUID playerId) {
        for (QueuePlayer queuePlayer : playersInQueue) {
            if (queuePlayer.getPlayerId().equals(playerId)) {
                return queuePlayer;
            }
        }
        return null;
    }

    public List<QueuePlayer> getOfflineQueuedPlayers() {
        final List<QueuePlayer> offlinePlayers = new ArrayList<>();

        for (QueuePlayer queuePlayer : playersInQueue) {
            if (!queuePlayer.isOnline()) {
                offlinePlayers.add(queuePlayer);
            }
        }
        return offlinePlayers;
    }
}
