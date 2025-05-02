package io.github.swagree.repokepvp.entity;

import java.util.UUID;

public class PlayerScore {
    private final UUID uuid;
    private final String playerName;
    private final int score;

    public PlayerScore(UUID uuid, String playerName, int score) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.score = score;
    }

    // Getter 方法
    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    // 可选：toString() 方法用于调试
    @Override
    public String toString() {
        return "PlayerScore{" +
                "uuid=" + uuid +
                ", playerName='" + playerName + '\'' +
                ", score=" + score +
                '}';
    }

    // 可选：equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerScore that = (PlayerScore) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}