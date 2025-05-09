package io.github.swagree.repokepvp.state.impl;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import io.github.swagree.repokepvp.Main;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class InBattleState extends BaseState {
    private final ServiceManager serviceManager;
    private final Member opponent;

    public InBattleState(Member member, ServiceManager serviceManager, Member opponent) {
        super(member, serviceManager);
        this.serviceManager = serviceManager;
        this.opponent = opponent;

    }

    @Override
    public void handleJoinQueue(String configName) {
        member.getBukkitPlayer().sendMessage(ChatColor.RED + "你正在对战中，无法加入匹配");
    }

    @Override
    public void handleMatchFound(Member opponent) {
        member.getBukkitPlayer().sendMessage(ChatColor.RED + "错误: 已经在战斗中");
    }

    @Override
    public void handleStartBattle(BattleRules rules) {
        try {
            // 验证对手有效性
            if (opponent == null || !opponent.getBukkitPlayer().isOnline()) {
                throw new IllegalStateException("对手无效或已离线");
            }

            // 启动战斗
            serviceManager.getBattleStarter().initiateBattle(
                    member.getBukkitPlayer(),
                    opponent.getBukkitPlayer(),
                    rules
            );

            // 记录战斗状态
            serviceManager.getBattleStateManager().addToBattle(member.getPlayerId());
            serviceManager.getBattleStateManager().addToBattle(opponent.getPlayerId());

        } catch (Exception e) {
            member.getBukkitPlayer().sendMessage(ChatColor.RED + "战斗开始失败: " + e.getMessage());
            handleEndBattle();
        }
    }

    @Override
    public void handleEndBattle() {
        // 清理当前玩家
        serviceManager.getBattleStateManager().removeFromBattle(member.getPlayerId());
        addCooldown(member);
        member.transitionTo(new IdleState(member, serviceManager));

        // 清理对手玩家
        if (opponent != null) {
            serviceManager.getBattleStateManager().removeFromBattle(opponent.getPlayerId());
            addCooldown(opponent);
            if (opponent.getCurrentState() instanceof InBattleState) {
                opponent.transitionTo(new IdleState(opponent, serviceManager));
            }
        }
    }

    private void addCooldown(Member target) {
        UUID playerId = target.getPlayerId();
        serviceManager.getBattleStateManager().addCooldown(playerId);
        new BukkitRunnable() {
            @Override
            public void run() {
                serviceManager.getBattleStateManager().removeCooldown(playerId);
            }
        }.runTaskLater(Main.getInstance(), serviceManager.getBattleStateManager().getCooldownDuration());
    }
}