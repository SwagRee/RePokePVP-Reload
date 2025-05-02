package io.github.swagree.repokepvp.command.sub;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

// SubCommand.java - 子命令接口
public interface ISubCommand {
    String getName();
    default List<String> getAliases() { return Collections.emptyList(); }
    default String getPermission() { return null; }
    boolean execute(CommandSender sender, String[] args);
}
