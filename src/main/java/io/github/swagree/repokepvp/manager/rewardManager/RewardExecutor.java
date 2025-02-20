package io.github.swagree.repokepvp.manager.rewardManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;

public class RewardExecutor {
    public static void executeCommands(Player player, List<String> commands) {
        commands.forEach(command -> {
            String formatted = command
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
        });
    }
}