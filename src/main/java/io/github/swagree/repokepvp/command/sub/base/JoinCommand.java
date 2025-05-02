package io.github.swagree.repokepvp.command.sub.base;

import io.github.swagree.repokepvp.command.sub.BasePlayerCommand;
import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

// JoinCommand.java - 加入队列
public class JoinCommand extends BasePlayerCommand {
    public JoinCommand(ServiceManager serviceManager) {
        super(serviceManager);
    }

    @Override public String getName() { return "join"; }
    @Override public List<String> getAliases() { return Arrays.asList("j", "queue"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = checkPlayer(sender);
        if (player == null) return false;

        String configName = args.length > 0 ? args[0] : "default";
        Member member = serviceManager.getMemberManager().getMember(player);
        member.joinQueue(configName);
        return true;
    }
}
