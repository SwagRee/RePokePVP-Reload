package io.github.swagree.repokepvp.manager.matchManager;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MatchQueueManager {
    private final Queue<UUID> battleQueue = new ConcurrentLinkedQueue<>();
    
    public void addToQueue(UUID playerId) {
        battleQueue.offer(playerId);
    }
    
    public boolean isInQueue(UUID playerId) {
        return battleQueue.contains(playerId);
    }
    
    public UUID pollFromQueue() {
        return battleQueue.poll();
    }
    
    public int getQueueSize() {
        return battleQueue.size();
    }
}