package io.github.swagree.repokepvp.manager.matchManager;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.BattleQuery;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.EnumBattleQueryResponse;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.util.PixelmonPlayerUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.entity.Player;

public class BattleStarter {
    public void initiateBattle(Player player1, Player player2, BattleRules rules) {
        EntityPlayerMP ep1 = PixelmonPlayerUtils.getUniquePlayerStartingWith(player1.getName());
        EntityPlayerMP ep2 = PixelmonPlayerUtils.getUniquePlayerStartingWith(player2.getName());

        PlayerParticipant participant1 = new PlayerParticipant(ep1);
        PlayerParticipant participant2 = new PlayerParticipant(ep2);

        EntityPixelmon activePokemon1 = participant1.getStorage().getAndSendOutFirstAblePokemon(ep1);
        EntityPixelmon activePokemon2 = participant2.getStorage().getAndSendOutFirstAblePokemon(ep2);

        BattleQuery battleQuery = new BattleQuery(
                ep1,
                activePokemon1,
                ep2,
                activePokemon2
        );

        battleQuery.proposeRules(ep1, rules);
        battleQuery.acceptQuery(ep2, EnumBattleQueryResponse.Accept);
    }
}