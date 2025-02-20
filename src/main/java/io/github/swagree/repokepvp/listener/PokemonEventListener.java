package io.github.swagree.repokepvp.listener;

import catserver.api.bukkit.event.ForgeEvent;
import com.pixelmonmod.pixelmon.api.events.HeldItemChangedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import io.github.swagree.repokepvp.Main;
import io.github.swagree.repokepvp.command.PlayerCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PokemonEventListener implements Listener {


    @EventHandler
    public void onBattleStarted(ForgeEvent event) {
        if (event.getForgeEvent() instanceof BattleEndEvent) {
            handleBattleEnd((BattleEndEvent) event.getForgeEvent());
        }
    }

    @EventHandler
    public void onHeldItemChange(ForgeEvent event){
        if(event.getForgeEvent() instanceof HeldItemChangedEvent){
            HeldItemChangedEvent e = (HeldItemChangedEvent)event.getForgeEvent();

            UUID uniqueID = ((HeldItemChangedEvent) event.getForgeEvent()).player.getUniqueID();

            if(PlayerCommand.battleQueue.contains(uniqueID)){
                e.setCanceled(true);
                e.pokemon.setHeldItem(null);
                Bukkit.getPlayer(uniqueID).sendMessage(ChatColor.RED+"匹配之中切换携带物,给你没收了");
            }
        }
    }

    private void handleBattleEnd(BattleEndEvent forgeEvent) {

        List<UUID> validPlayers = new ArrayList<>();

        for (BattleParticipant participant : forgeEvent.bc.participants) {
            if (participant instanceof PlayerParticipant) {
                PlayerParticipant pp = (PlayerParticipant) participant;
                UUID playerId = pp.player.getUniqueID();
                if (PlayerCommand.isInBattle(playerId)) {
                    validPlayers.add(playerId);
                }
            }
        }

        if (validPlayers.isEmpty()) return;

        List<PlayerParticipant> winners = new ArrayList<>();
        List<PlayerParticipant> losers = new ArrayList<>();

        for (BattleParticipant participant : forgeEvent.bc.participants) {
            if (!(participant instanceof PlayerParticipant)) continue;

            PlayerParticipant pp = (PlayerParticipant) participant;
            UUID playerId = pp.player.getUniqueID();

            if (!validPlayers.contains(playerId)) continue;

            BattleResults result = forgeEvent.results.get(participant);
            if (result == BattleResults.VICTORY) {
                Main.dailyWinManager.handleVictory(Bukkit.getPlayer(pp.player.getUniqueID()));
                winners.add(pp);
            } else if (isDefeatResult(result)) {
                losers.add(pp);
            }
            Main.dailyWinManager.handleBattleEnd(Bukkit.getPlayer(pp.player.getUniqueID()));

        }

        if (!winners.isEmpty()) executeCommands(winners, "WinCommand");
        if (!losers.isEmpty()) executeCommands(losers, "LoseCommand");

        validPlayers.forEach(PlayerCommand::removeFromBattle);
    }


    private boolean isDefeatResult(BattleResults result) {
        return result == BattleResults.DEFEAT || result == BattleResults.FLEE;
    }

    private void executeCommands(List<PlayerParticipant> participants, String commandType) {
        List<String> commands = Main.INSTANCE.getConfig().getStringList(commandType);
        participants.stream().map(pp -> pp.player.getName()).filter(name -> !name.isEmpty()).forEach(playerName -> {
            commands.forEach(command -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", playerName));
            });
        });
    }
}