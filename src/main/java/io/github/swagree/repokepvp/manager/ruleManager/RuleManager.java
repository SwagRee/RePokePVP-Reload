package io.github.swagree.repokepvp.manager.ruleManager;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.rules.clauses.*;
import com.pixelmonmod.pixelmon.battles.rules.clauses.tiers.EnumTier;
import com.pixelmonmod.pixelmon.battles.rules.clauses.tiers.Tier;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.*;
import com.pixelmonmod.pixelmon.enums.EnumOldGenMode;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import io.github.swagree.repokepvp.util.YmlUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuleManager {
    public BattleRules createBattleRules(String configName) {

        FileConfiguration config = YmlUtil.getConfig("pvplist/" + configName);

        BattleRules rules = new BattleRules();

        // 基础规则
        toSetBaseRule(rules, config);

        // 对战类型
        toSetBattleType(config, rules);

        // 世代模式
        toSetBattleOldGen(config, rules);

        // 分级
        toSetBattleTier(config, rules);

        // 对战条款
        if (config.getBoolean("openClauses", false)) {
            toSetBattleClauses(config, rules);
        }

        return rules;
    }

    private static void toSetBattleClauses(FileConfiguration config, BattleRules rules) {
        List<BattleClause> clauses = new ArrayList<>();
        addClausesToBattle(config.getCurrentPath(), rules);
        rules.setNewClauses(clauses);
    }

    private static void toSetBattleTier(FileConfiguration config, BattleRules rules) {
        String tier = config.getString("rule.tier", "Unrestricted");
        rules.tier = Arrays.stream(EnumTier.values())
                .filter(t -> t.name().equalsIgnoreCase(tier))
                .findFirst()
                .map(t -> new Tier(t.getTierID()))
                .orElse(new Tier(EnumTier.Unrestricted.getTierID()));
    }

    private static void toSetBattleOldGen(FileConfiguration config, BattleRules rules) {
        String oldGen = config.getString("rule.oldgen", "none");
        rules.oldgen = EnumOldGenMode.valueOf(capitalizeFirst(oldGen));
    }

    private static void toSetBattleType(FileConfiguration config, BattleRules rules) {
        String battleType = config.getString("rule.battleType", "Single");
        rules.battleType = EnumBattleType.valueOf(battleType);
    }

    private static void toSetBaseRule(BattleRules rules, FileConfiguration config) {
        rules.levelCap = config.getInt("rule.level", 50);
        rules.numPokemon = config.getInt("rule.numPokemon", 3);
        rules.turnTime = config.getInt("rule.turnTime", 30) * 20;
        rules.teamSelectTime = config.getInt("rule.teamSelectTime", 60) * 20;
        rules.fullHeal = config.getBoolean("rule.fullHeal", true);
        rules.raiseToCap = config.getBoolean("rule.raiseToCap", true);
        rules.teamPreview = config.getBoolean("rule.teamPreview", true);
    }

    private static void addClausesToBattle(String path, BattleRules rules) {
        ArrayList<BattleClause> clauses = new ArrayList<>();
        FileConfiguration fileConfiguration = YmlUtil.getConfig(path);
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.bagClause"))) {
            clauses.add(new BattleClause("bag"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.batonPassClause"))) {
            clauses.add(new MoveClause("batonpass", new String[]{"Baton Pass"}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.maxOneBatonPass"))) {
            clauses.add(new BattleClause("batonpass1"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.chatter"))) {
            clauses.add(new MoveClause("chatter", new String[]{"Chatter"}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.drizzle"))) {
            clauses.add(new AbilityClause("drizzle", new Class[]{Drizzle.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.drizzleSwim"))) {
            clauses.add(new AbilityComboClause("drizzleswim", new Class[]{Drizzle.class, SwiftSwim.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.drought"))) {
            clauses.add(new AbilityClause("drought", new Class[]{Drought.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.endlessBattle"))) {
            clauses.add(new BattleClauseSingleAll("endlessbattle", new BattleClause[]{new ItemPreventClause("", new EnumHeldItems[]{EnumHeldItems.leppa}), new MoveClause("", new String[]{"Recycle"}), new MoveClause("", new String[]{"Fling", "Heal Pulse", "Pain Split"})}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.evasionAbility"))) {
            clauses.add(new AbilityClause("evasionability", new Class[]{SandVeil.class, SnowCloak.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.evasion"))) {
            clauses.add(new MoveClause("evasion", new String[]{"Double Team", "Minimize"}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.forfeitClause"))) {
            clauses.add(new BattleClause("forfeit"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.inverseBattle"))) {
            clauses.add(new BattleClause("inverse"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.noDuplicateItems"))) {
            clauses.add(new BattleClause("item"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.legendBan"))) {
            clauses.add(new PokemonClause("legendary", EnumSpecies.LEGENDARY_ENUMS));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.megaStone"))) {
            clauses.add(new ItemPreventClause("mega", new EnumHeldItems[]{EnumHeldItems.megaStone}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.moody"))) {
            clauses.add(new AbilityClause("moody", new Class[]{Moody.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.ohKo"))) {
            clauses.add(new MoveClause("ohko", new String[]{"Fissure", "Guillotine", "Horn Drill", "Sheer Cold"}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.sandStream"))) {
            clauses.add(new AbilityClause("sandstream", new Class[]{SandStream.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.shadowTag"))) {
            clauses.add(new AbilityClause("shadowtag", new Class[]{ShadowTag.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.skyBattle"))) {
            clauses.add(new BattleClause("sky"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.sleepClause"))) {
            clauses.add(new BattleClause("sleep"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.smashPass"))) {
            clauses.add(new BattleClauseSingleAll("smashpass", new BattleClause[]{new MoveClause("batonpass", new String[]{"Baton Pass"}), new MoveClause("", new String[]{"Shell Smash"})}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.snowWarning"))) {
            clauses.add(new AbilityClause("snowwarning", new Class[]{SnowWarning.class}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.soulDew"))) {
            clauses.add(new BattleClauseSingleAll("souldew", new BattleClause[]{new PokemonClause("", new EnumSpecies[]{EnumSpecies.Latias, EnumSpecies.Latios}), new ItemPreventClause("", new EnumHeldItems[]{EnumHeldItems.soulDew})}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.multipleSpecies"))) {
            clauses.add(new BattleClause("pokemon"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.speedPass"))) {
            clauses.add(new BattleClause("speedpass"));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.swagger"))) {
            clauses.add(new MoveClause("swagger", new String[]{"Swagger"}));
        }
        if ("true".equalsIgnoreCase(fileConfiguration.getString("Clauses.weatherSpeed"))) {
            clauses.add(new BattleClauseAny("weatherspeed", new BattleClause[]{new AbilityComboClause("drizzleswim", new Class[]{Drizzle.class, SwiftSwim.class}), new AbilityComboClause("", new Class[]{Drought.class, Chlorophyll.class}), new AbilityComboClause("", new Class[]{SandStream.class, SandRush.class})}));
        }
        rules.setNewClauses(clauses);
    }

    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }


}
