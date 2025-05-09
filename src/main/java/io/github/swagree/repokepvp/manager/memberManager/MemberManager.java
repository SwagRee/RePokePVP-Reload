package io.github.swagree.repokepvp.manager.memberManager;

import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.swagree.repokepvp.staticPackage.PluginStatic.members;

public class MemberManager {
    ServiceManager serviceManager;
    public MemberManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }


    public Member getMember(Player player) {
        return members.computeIfAbsent(player.getUniqueId(), k -> new Member(player));
    }

    public Member getMember(UUID playerId) {
        return members.get(playerId);
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
    }

    public void clearAll() {
        members.clear();
    }
}