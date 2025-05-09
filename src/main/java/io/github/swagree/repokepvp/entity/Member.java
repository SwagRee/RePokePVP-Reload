package io.github.swagree.repokepvp.entity;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import io.github.swagree.repokepvp.Main;
import io.github.swagree.repokepvp.manager.ServiceManager;
import io.github.swagree.repokepvp.state.impl.IdleState;
import io.github.swagree.repokepvp.state.impl.InBattleState;
import io.github.swagree.repokepvp.state.impl.InQueueState;
import io.github.swagree.repokepvp.state.MemberState;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Member {
    private final UUID playerId;
    private final String playerName;
    private MemberState currentState;
    private final Player bukkitPlayer;

    public Member(Player player) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.bukkitPlayer = player;
        this.currentState  = new IdleState(this,new ServiceManager(Main.getInstance())); // 初始状态为闲置状态

    }
    public MemberState getCurrentState() {
        return currentState;
    }
    // 状态转换方法
    public void transitionTo(MemberState currentState) {
        this.currentState = currentState;
    }

    // 业务方法委托给当前状态处理
    public void joinQueue(String configName) {
        currentState.handleJoinQueue(configName);
    }

    public void matchFound(Member opponent) {
        currentState.handleMatchFound(opponent);
    }

    public void startBattle(BattleRules rules) {
        currentState.handleStartBattle(rules);
    }

    public void endBattle() {
        currentState.handleEndBattle();
    }

    // Getter方法
    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    // 状态检查方法
    public boolean isIdle() {
        return currentState instanceof IdleState;
    }

    public boolean isInQueue() {
        return currentState instanceof InQueueState;
    }

    public boolean isInBattle() {
        return currentState instanceof InBattleState;
    }
}