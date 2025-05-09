package io.github.swagree.repokepvp.manager.matchManager;

import io.github.swagree.repokepvp.Main;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.swagree.repokepvp.staticPackage.PluginStatic.battleQueue;

public class MatchQueueManager {
    // 队列相关（改用普通队列）
    private final Main plugin;
    private final ServiceManager serviceManager;
    private BukkitTask maintenanceTask;

    // 冷却系统（改用普通 Map）
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public MatchQueueManager(Main plugin, ServiceManager serviceManager) {
        this.plugin = plugin;
        this.serviceManager = serviceManager;
        startQueueMaintenance();
        startCooldownCleanup();
    }

    public void addToQueue(UUID playerId,String configName) {
        // 1. 冷却检查
        if (isOnCooldown(playerId)) {
            Bukkit.getPlayer(playerId).sendMessage("cd中");
            return;
        }

        // 2. 获取 Member 和玩家对象
        Member member = serviceManager.getMemberManager().getMember(playerId);
        if (member == null) {
            plugin.getLogger().warning("玩家 " + playerId + " 的 Member 对象不存在");
            return;
        }

        Player player = member.getBukkitPlayer();
        if (player == null || !player.isOnline()) {
            plugin.getLogger().warning("玩家 " + playerId + " 不在线");
            return;
        }

        battleQueue.offer(playerId);
        plugin.getLogger().info("玩家加入队列: " + player.getName() + " (" + playerId + ")");

        // 每次入队后立即检查
        checkQueue(configName);
    }

    public void checkQueue(String configName) {
        // 1. 如果队列不足 2 人，直接返回
        plugin.getLogger().info("检查队列，当前大小: " + battleQueue.size());

        if (battleQueue.size() < 2) {
            return;
        }
        // 2. 取出前两人
        UUID player1Id = battleQueue.poll();
        UUID player2Id = battleQueue.poll();

        Member member1 = serviceManager.getMemberManager().getMember(player1Id);
        Member member2 = serviceManager.getMemberManager().getMember(player2Id);

        member1.matchFound(member2);
        member2.matchFound(member1);


        member1.getCurrentState().handleStartBattle(serviceManager.getRuleManager().createBattleRules(configName));
//        member2.getCurrentState().handleStartBattle(serviceManager.getRuleManager().createBattleRules("default"));
    }

    // ================= 冷却系统 =================
    public void addCooldown(UUID playerId, int seconds) {
        cooldowns.put(playerId, System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean isOnCooldown(UUID playerId) {
        Long expireTime = cooldowns.get(playerId);
        if (expireTime == null) return false;

        if (System.currentTimeMillis() > expireTime) {
            cooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    private void startCooldownCleanup() {
        // 主线程清理冷却
        Bukkit.getScheduler().runTaskTimer(plugin, () ->
                cooldowns.entrySet().removeIf(entry ->
                        System.currentTimeMillis() > entry.getValue()
                ), 1200L, 1200L
        );
    }

    // ================= 维护任务 =================
    private void startQueueMaintenance() {
        // 主线程清理离线玩家
        maintenanceTask = Bukkit.getScheduler().runTaskTimer(plugin, () ->
                battleQueue.removeIf(uuid ->
                        Bukkit.getPlayer(uuid) == null || !Bukkit.getPlayer(uuid).isOnline()
                ), 1200L, 1200L
        );
    }

    public void stopQueueMaintenance() {
        if (maintenanceTask != null) {
            maintenanceTask.cancel();
        }
    }

    // ================= 其他方法 =================
    public void removeFromQueue(UUID playerId) {
        battleQueue.remove(playerId);
    }

    public String getQueueStatus() {
        return "当前队列 (" + battleQueue.size() + "人): " + battleQueue.stream()
                .map(uuid -> Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : uuid.toString())
                .collect(Collectors.joining(", "));
    }
}