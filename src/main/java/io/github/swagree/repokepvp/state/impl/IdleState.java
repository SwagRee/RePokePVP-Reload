package io.github.swagree.repokepvp.state.impl;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import io.github.swagree.repokepvp.manager.memberManager.MemberManager;
import io.github.swagree.repokepvp.util.YmlUtil;
import org.bukkit.ChatColor;

public class IdleState extends BaseState {
    private final ServiceManager serviceManager;

    public IdleState(Member member, ServiceManager serviceManager) {
        super(member,serviceManager);
        this.serviceManager = serviceManager;
    }

    @Override
    public void handleJoinQueue(String configName) {
        // 验证玩家队伍合法性
        if (!serviceManager.getBattleValidator().validatePlayer(
                member.getBukkitPlayer(),
                YmlUtil.getConfig("pvplist/" + configName))) {
            return;
        }
        // 加入匹配队列

        member.getBukkitPlayer().sendMessage(ChatColor.GREEN + "已加入匹配队列");
        member.transitionTo(new InQueueState(member, serviceManager));
        serviceManager.getMatchQueueManager().addToQueue(member.getPlayerId(),configName);


    }

    @Override
    public void handleMatchFound(Member opponent) {
        // 闲置状态不应该收到匹配成功通知
        member.getBukkitPlayer().sendMessage(ChatColor.RED + "错误: 不在匹配状态");
    }

    @Override
    public void handleStartBattle(BattleRules rules) {
        // 闲置状态不应该开始战斗
        member.getBukkitPlayer().sendMessage(ChatColor.RED + "错误: 不在战斗状态");
    }

    @Override
    public void handleEndBattle() {
        // 闲置状态不需要结束战斗
    }


}