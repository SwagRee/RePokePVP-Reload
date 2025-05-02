package io.github.swagree.repokepvp.state.impl;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import io.github.swagree.Main;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class InBattleState extends BaseState {
    private final ServiceManager serviceManager;

    public InBattleState(Member member, ServiceManager serviceManager) {
        super(member, serviceManager);
        this.serviceManager = serviceManager;
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
            serviceManager.getBattleStarter().initiateBattle(
                    member.getBukkitPlayer(),
                    serviceManager.getMemberManager().getMember(member.getPlayerId()).getBukkitPlayer(),
                    rules
            );
            serviceManager.getBattleStateManager().addToBattle(member.getPlayerId());
        } catch (Exception e) {
            member.getBukkitPlayer().sendMessage(ChatColor.RED + "战斗开始失败");
            handleEndBattle(); // 确保状态正确清理
            throw new RuntimeException("战斗初始化失败", e);
        }
    }

    @Override
    public void handleEndBattle() {
        // 从战斗状态中移除
        serviceManager.getBattleStateManager().removeFromBattle(member.getPlayerId());

        // 添加冷却时间
        serviceManager.getBattleStateManager().addCooldown(member.getPlayerId());

        // 设置冷却到期回调
        new BukkitRunnable() {
            @Override
            public void run() {
                serviceManager.getBattleStateManager().removeCooldown(member.getPlayerId());
            }
        }.runTaskLater(Main.getInstance(),
                serviceManager.getBattleStateManager().getCooldownDuration());

        // 转换回闲置状态
        member.transitionTo(new IdleState(member, serviceManager));
    }
}