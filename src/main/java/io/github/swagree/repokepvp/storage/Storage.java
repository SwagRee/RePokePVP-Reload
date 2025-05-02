package io.github.swagree.repokepvp.storage;

import io.github.swagree.repokepvp.entity.PlayerScore;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface Storage {
    boolean checkFirstWin(UUID uuid, LocalDate today) throws SQLException;
    void updateWinRecord(UUID uuid, LocalDate today,Integer totalMatch,Integer wins) throws SQLException;
    void close() throws SQLException;


    void addScore(UUID uuid, int points) throws SQLException;
    int getScore(UUID uuid) throws SQLException;
    List<PlayerScore> getTopPlayers(int limit) throws SQLException;
    int getWins(UUID uuid) throws SQLException;
    int getTotalMatch(UUID uuid) throws SQLException;

    void addTotalMatch(UUID uuid) throws SQLException;
    void addWins(UUID uuid) throws SQLException;
    void reduceScore(UUID uuid, int points) throws SQLException;


}
