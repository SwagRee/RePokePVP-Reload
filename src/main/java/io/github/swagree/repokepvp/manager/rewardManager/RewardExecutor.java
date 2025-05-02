package io.github.swagree.repokepvp.manager.rewardManager;

import io.github.swagree.Main;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
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
    public static void executeCommands(List<Member> members, String commandType) {
        List<String> commands = Main.getInstance().getConfig().getStringList(commandType);
        members.stream()
                .map(Member::getPlayerName)
                .filter(name -> !name.isEmpty())
                .forEach(playerName -> {
                    commands.forEach(command -> {
                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                command.replace("%player%", playerName)
                        );
                    });
                });
    }
}