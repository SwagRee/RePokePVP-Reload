package io.github.swagree.repokepvp.command.sub.info;

import io.github.swagree.repokepvp.command.sub.ISubCommand;
import io.github.swagree.repokepvp.entity.PlayerScore;
import io.github.swagree.repokepvp.manager.ServiceManager;
import io.github.swagree.repokepvp.manager.dataManager.BattleManager;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TopCommand implements ISubCommand {
    // 依赖项
    private final ServiceManager serviceManager;

    // 构造函数（必须的依赖注入）
    public TopCommand(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    // 命令名称
    @Override
    public String getName() {
        return "top";
    }

    // 命令别名
    @Override
    public List<String> getAliases() {
        return Arrays.asList("ranking", "leaderboard");
    }

    // 权限要求（无需权限）
    @Override
    public String getPermission() {
        return null;
    }

    // 执行逻辑
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            BattleManager battleManager = serviceManager.getBattleManager();
            List<PlayerScore> topPlayers = battleManager.getTop();

            if (topPlayers.isEmpty()) {
                sender.sendMessage("§e暂无排行榜数据");
                return true;
            }

            sender.sendMessage("§6===== 排行榜 TOP" + topPlayers.size() + " =====");
            for (int i = 0; i < topPlayers.size(); i++) {
                PlayerScore score = topPlayers.get(i);
                UUID uuid = score.getUuid();

                int wins = battleManager.getWins(uuid);
                int totalMatches = battleManager.getTotalMatch(uuid);
                int points = battleManager.getScore(uuid);

                double winRate = (totalMatches == 0) ? 0.0 : (wins * 100.0 / totalMatches);
                String formattedWinRate = String.format("§a%.2f%%", winRate);

                String entry = String.format(
                        "§e#%d §b%s §f- 积分: §3%d §f胜场: §a%d §f胜率: %s",
                        i + 1, score.getPlayerName(), points, wins, formattedWinRate
                );
                sender.sendMessage(entry);
            }
            return true;
        } catch (SQLException e) {
            // 异常处理...
            return false;
        }
    }
}