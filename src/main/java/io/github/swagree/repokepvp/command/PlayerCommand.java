package io.github.swagree.repokepvp.command;

import io.github.swagree.repokepvp.command.sub.ISubCommand;
import io.github.swagree.repokepvp.command.sub.base.JoinCommand;
import io.github.swagree.repokepvp.command.sub.admin.ReloadCommand;
import io.github.swagree.repokepvp.command.sub.info.TopCommand;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class PlayerCommand implements CommandExecutor {
    private final Map<String, ISubCommand> subCommands = new HashMap<>();

    public PlayerCommand(ServiceManager serviceManager) {
        registerCommands(serviceManager);
    }

    private void registerCommands(ServiceManager serviceManager) {
        register(new ReloadCommand(serviceManager));
        register(new JoinCommand(serviceManager));
        register(new TopCommand(serviceManager));
    }

    private void register(ISubCommand command) {
        subCommands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            subCommands.put(alias.toLowerCase(), command);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return false;

        String subCmd = args[0].toLowerCase();
        ISubCommand command = subCommands.get(subCmd);
        if (command == null) return false;

        if (!checkPermission(sender, command)) return true;
        return command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    private boolean checkPermission(CommandSender sender, ISubCommand command) {
        String perm = command.getPermission();
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage("§c你没有执行该命令的权限");
            return false;
        }
        return true;
    }
}