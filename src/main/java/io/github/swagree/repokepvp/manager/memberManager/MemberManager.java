package io.github.swagree.repokepvp.manager.memberManager;

import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemberManager {
    ServiceManager serviceManager;
    public MemberManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    private final Map<UUID, Member> members = new ConcurrentHashMap<>();

    public Member getMember(Player player) {
        return members.computeIfAbsent(player.getUniqueId(), k -> new Member(player,serviceManager));
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