package io.github.swagree.repokepvp.manager.matchManager;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.swagree.repokepvp.staticPackage.PluginStatic;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BattleValidator {
    public boolean validatePlayer(Player player, FileConfiguration config) {
        PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
        int totalPokemon = 0;
        if (party == null) return false;

        List<String> errorMessages = new ArrayList<>();
        boolean isValid = true;

        for (int i = 0; i < 6; i++) {
            Pokemon pokemon = party.get(i);
            if (pokemon == null) continue;

            // 神兽/幻兽/异兽/悖论检查
            if (pokemon.isLegendary()) {
                totalPokemon++;
            }
            for (EnumSpecies enumSpecies : PluginStatic.UltraALL) {
                if (enumSpecies.getLocalizedName().equalsIgnoreCase(pokemon.getLocalizedName())) {
                    totalPokemon++;
                }
            }

            // 宝可梦黑名单检查
            if (config.getStringList("rule.blackList.blackListPokemon").contains(pokemon.getLocalizedName())) {
                errorMessages.add("§c黑名单宝可梦: " + pokemon.getLocalizedName());
                isValid = false;
            }

            // 技能检查
            for (Attack attack : pokemon.getMoveset().attacks) {
                if(attack == null) continue;
                if (config.getStringList("rule.blackList.blackListMove").contains(attack.getActualMove().getLocalizedName())) {
                    errorMessages.add("§c禁用技能: " + attack.getActualMove().getLocalizedName());
                    isValid = false;
                }
            }

            // 携带物检查
            String heldItem = pokemon.getHeldItemAsItemHeld().getLocalizedName();
            if (config.getStringList("rule.blackList.blackListHeldItem").contains(heldItem)) {
                errorMessages.add("§c禁用携带物: " + heldItem);
                isValid = false;
            }

            // 特性检查
            String ability = pokemon.getAbility().getLocalizedName();
            if (config.getStringList("rule.blackList.blackListAbility").contains(ability)) {
                errorMessages.add("§c禁用特性: " + ability);
                isValid = false;
            }

            // 形态检查
            String form = pokemon.getFormEnum().getLocalizedName();
            if (config.getStringList("rule.blackList.blackListForm").contains(form)) {
                errorMessages.add("§c禁用形态: " + form);
                isValid = false;
            }
        }

        if (!isValid) {
            errorMessages.forEach(player::sendMessage);
            return false;
        }

        if(totalPokemon > 1) {
            player.sendMessage(ChatColor.RED + "限制1只神兽/幻兽/异兽/悖论");
            return false;
        }
        return true;
    }
}