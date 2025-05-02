package io.github.swagree.repokepvp.state.impl;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.ChatColor;

public class InQueueState extends BaseState {
    public InQueueState(Member member, ServiceManager serviceManager) {
        super(member,serviceManager);
    }

    @Override
    public void handleJoinQueue(String configName) {
        member.getBukkitPlayer().sendMessage(ChatColor.YELLOW + "你已经在匹配队列中");
    }

    @Override
    public void handleMatchFound(Member opponent) {
        member.getBukkitPlayer().sendMessage(ChatColor.GREEN + "匹配成功! 对手: " + opponent.getPlayerName());
        member.transitionTo(new InBattleState(member,serviceManager));
    }

    @Override
    public void handleStartBattle(BattleRules rules) {
        // 匹配中状态不应该直接开始战斗，应该先通过matchFound
        member.getBukkitPlayer().sendMessage(ChatColor.RED + "错误: 战斗流程异常");
    }

    @Override
    public void handleEndBattle() {
        // 匹配中状态不需要结束战斗
    }
}