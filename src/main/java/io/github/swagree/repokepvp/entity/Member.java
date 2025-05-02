package io.github.swagree.repokepvp.entity;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import io.github.swagree.Main;
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
    private MemberState state;
    private final Player bukkitPlayer;

    public Member(Player player, ServiceManager serviceManager) { // 通过构造函数注入
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.bukkitPlayer = player;
        // 依赖注入
        this.state = new IdleState(this, serviceManager); // 使用注入的 ServiceManager
    }
    // 状态转换方法
    public void transitionTo(MemberState state) {
        this.state = state;
    }

    // 业务方法委托给当前状态处理
    public void joinQueue(String configName) {
        state.handleJoinQueue(configName);
    }

    public void matchFound(Member opponent) {
        state.handleMatchFound(opponent);
    }

    public void startBattle(BattleRules rules) {
        state.handleStartBattle(rules);
    }

    public void endBattle() {
        state.handleEndBattle();
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
        return state instanceof IdleState;
    }

    public boolean isInQueue() {
        return state instanceof InQueueState;
    }

    public boolean isInBattle() {
        return state instanceof InBattleState;
    }
}