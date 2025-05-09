package io.github.swagree.repokepvp.listener;

import catserver.api.bukkit.event.ForgeEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import io.github.swagree.repokepvp.manager.rewardManager.RewardExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PokemonEventListener implements Listener {

    private final ServiceManager serviceManager;

    public PokemonEventListener(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @EventHandler
    public void onBattleEnd(ForgeEvent event) {
        if (event.getForgeEvent() instanceof BattleEndEvent) {
            handleBattleEnd((BattleEndEvent) event.getForgeEvent());
        }
    }

    private void handleBattleEnd(BattleEndEvent forgeEvent) {
        List<Member> participants = new ArrayList<>();

        // 收集所有参与战斗的玩家Member对象
        for (BattleParticipant participant : forgeEvent.bc.participants) {
            if (participant instanceof PlayerParticipant) {
                PlayerParticipant pp = (PlayerParticipant) participant;
                UUID playerId = pp.player.getUniqueID();
                Player player = Bukkit.getPlayer(playerId);

                if (player != null) {
                    Member member = serviceManager.getMemberManager().getMember(player);
                    if (member.isInBattle()) {
                        participants.add(member);
                    }
                }
            }
        }

        if (participants.isEmpty()) return;

        List<Member> winners = new ArrayList<>();
        List<Member> losers = new ArrayList<>();

        // 处理战斗结果
        for (Member member : participants) {
            BattleResults result = getParticipantResult(forgeEvent, member.getPlayerId());

            if (result == BattleResults.VICTORY) {
                handleVictory(member);
                winners.add(member);
            } else if (isDefeatResult(result)) {
                handleDefeat(member);
                losers.add(member);
            }

            handleBattleEnd(member);
        }

        // 执行胜利/失败命令
        if (!winners.isEmpty()) RewardExecutor.executeCommands(winners, "WinCommand");
        if (!losers.isEmpty()) RewardExecutor.executeCommands(losers, "LoseCommand");

        // 结束所有参与者的战斗状态
        participants.forEach(Member::endBattle);
    }

    private BattleResults getParticipantResult(BattleEndEvent event, UUID playerId) {
        for (BattleParticipant participant : event.bc.participants) {
            if (participant instanceof PlayerParticipant) {
                PlayerParticipant pp = (PlayerParticipant) participant;
                if (pp.player.getUniqueID().equals(playerId)) {
                    return event.results.get(participant);
                }
            }
        }
        return BattleResults.DEFEAT;
    }

    private boolean isDefeatResult(BattleResults result) {
        return result == BattleResults.DEFEAT || result == BattleResults.FLEE;
    }

    private void handleVictory(Member member) {
        serviceManager.getBattleManager().handleVictory(member.getBukkitPlayer());
        member.getBukkitPlayer().sendMessage(ChatColor.GREEN + "恭喜你赢得了比赛！");
    }

    private void handleDefeat(Member member) {
        serviceManager.getBattleManager().handleDefeat(member.getBukkitPlayer());
        member.getBukkitPlayer().sendMessage(ChatColor.RED + "很遗憾你输掉了比赛...");
    }

    private void handleBattleEnd(Member member) {
        serviceManager.getBattleManager().handleBattleEnd(member.getBukkitPlayer());
    }


}