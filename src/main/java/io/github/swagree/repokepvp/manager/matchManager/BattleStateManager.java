package io.github.swagree.repokepvp.manager.matchManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BattleStateManager {
    private final Map<UUID, Boolean> activeBattles = new ConcurrentHashMap<>();
    private final Set<UUID> cooldownPlayers = ConcurrentHashMap.newKeySet();
    private final int cooldownDuration = 10 * 20; // 10秒

    public void addToBattle(UUID playerId) {
        activeBattles.put(playerId, true);
    }

    public boolean isInBattle(UUID playerId) {
        return activeBattles.containsKey(playerId);
    }

    public void removeFromBattle(UUID playerId) {
        activeBattles.remove(playerId);
    }

    public void addCooldown(UUID playerId) {
        cooldownPlayers.add(playerId);
    }

    public boolean isInCooldown(UUID playerId) {
        return cooldownPlayers.contains(playerId);
    }

    public void removeCooldown(UUID playerId) {
        cooldownPlayers.remove(playerId);
    }

    public int getCooldownDuration() {
        return cooldownDuration;
    }
}