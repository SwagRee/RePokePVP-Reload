package io.github.swagree.repokepvp.command;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleQuery;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.rules.clauses.*;
import com.pixelmonmod.pixelmon.battles.rules.clauses.tiers.EnumTier;
import com.pixelmonmod.pixelmon.battles.rules.clauses.tiers.Tier;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.EnumBattleQueryResponse;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.*;
import com.pixelmonmod.pixelmon.enums.EnumOldGenMode;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.PixelmonPlayerUtils;
import io.github.swagree.repokepvp.Main;
import io.github.swagree.repokepvp.entity.PlayerScore;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class PlayerCommand implements CommandExecutor {
    public static final Queue<UUID> battleQueue = new ConcurrentLinkedQueue<>();
    private static final Set<UUID> cooldownPlayers = ConcurrentHashMap.newKeySet();
    private static final int QUEUE_DELAY_TICKS = 3 * 20; // 3秒
    private static final int COOLDOWN_DURATION = 10 * 20; // 10秒
    private static final Map<String, FileConfiguration> configCache = new ConcurrentHashMap<>();

    private static final Map<UUID, Boolean> activeBattles = new ConcurrentHashMap<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return false;

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "join":
                return handleJoin(sender, args);
            case "top":
                try {
                    List<PlayerScore> top = Main.dailyWinManager.getTop();
                    for (int i = 0; i < top.size(); i++) {
                        UUID uuid = top.get(i).getUuid(); // 直接使用 UUID，不依赖在线状态
                        String playerName = top.get(i).getPlayerName();

                        Integer wins = Main.dailyWinManager.getWins(uuid);
                        Integer totalMatch = Main.dailyWinManager.getTotalMatch(uuid);

                        // 调试输出，检查数据是否正确
                        System.out.println("[DEBUG] " + playerName + " | wins: " + wins + " | totalMatch: " + totalMatch);

                        // 计算胜率（百分比）
                        double winRate = (totalMatch == 0) ? 0.0 : (wins * 100.0 / totalMatch);
                        String winRateFormatted = String.format("%.2f%%", winRate);

                        sender.sendMessage("第" + (i + 1) + "名: " + playerName +
                                " 胜场: " + wins +
                                " 胜率: " + winRateFormatted);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("数据库查询失败", e);
                }

            default:
                return false;
        }
    }

    private boolean handleReload(CommandSender sender) {
        configCache.clear(); // 清空配置缓存
        try {
            // 重新加载所有配置文件
            File configDir = new File(Main.INSTANCE.getDataFolder(), "pvplist");
            if (!configDir.exists()) return false;

            for (File file : Objects.requireNonNull(configDir.listFiles((dir, name) -> name.endsWith(".yml")))) {
                String configName = file.getName().replace(".yml", "");
                configCache.put(configName, YamlConfiguration.loadConfiguration(file));
            }
            sender.sendMessage("配置重载完成");
            return true;
        } catch (Exception e) {
            sender.sendMessage("配置重载失败: " + e.getMessage());
            Main.INSTANCE.getLogger().severe("配置重载错误: " + e.getMessage());
            return false;
        }
    }

    private boolean handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以执行此命令");
            return false;
        }

        Player player = (Player) sender;
        String configName = (args.length > 1) ? args[1] : "default";
        String configPath = "pvplist/" + configName;

        FileConfiguration config = getConfig(configPath);
        if (config == null) {
            player.sendMessage("§c无效的对战配置");
            return false;
        }

        if (!validatePlayer(player, config)) {
            return false;
        }

        return queuePlayer(player, configName);
    }

    private FileConfiguration getConfig(String path) {
        return configCache.computeIfAbsent(path, k -> {
            File file = new File(Main.INSTANCE.getDataFolder(), path + ".yml");
            if (!file.exists()) {
                try {
                    Main.INSTANCE.saveResource(path + ".yml", false);
                } catch (IllegalArgumentException e) {
                    Main.INSTANCE.getLogger().warning("配置文件不存在: " + path);
                    return null;
                }
            }
            return YamlConfiguration.loadConfiguration(file);
        });
    }
    public static final EnumSpecies[] UltraALL = new EnumSpecies[]{
            EnumSpecies.Buzzwole, EnumSpecies.Pheromosa, EnumSpecies.Xurkitree,
            EnumSpecies.Celesteela, EnumSpecies.Guzzlord, EnumSpecies.Kartana,
            EnumSpecies.Blacephalon, EnumSpecies.Poipole, EnumSpecies.Naganadel,
            EnumSpecies.Stakataka
    };

    private boolean validatePlayer(Player player, FileConfiguration config) {
        PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
        Integer totalPokemon = 0;
        if (party == null) return false;

        List<String> errorMessages = new ArrayList<>();
        boolean isValid = true;

        // 黑名单检查
        for (int i = 0; i < 6; i++) {
            Pokemon pokemon = party.get(i);

            if (pokemon == null) continue;

            if (pokemon.isLegendary()) {
                totalPokemon++;
            }
            for (EnumSpecies enumSpecies : UltraALL) {
                if (enumSpecies.getLocalizedName().equalsIgnoreCase(pokemon.getLocalizedName())) {
                    totalPokemon++;
                }
            }
            // 宝可梦黑名单
            if (config.getStringList("rule.blackList.blackListPokemon").contains(pokemon.getLocalizedName())) {
                errorMessages.add("§c黑名单宝可梦: " + pokemon.getLocalizedName());
                isValid = false;
            }

            // 技能检查
            for (Attack attack : pokemon.getMoveset().attacks) {
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

        if(totalPokemon>1){
            player.sendMessage(ChatColor.RED+"限制1只神兽/幻兽/异兽/悖论");
        }
        return true;
    }

    private boolean queuePlayer(Player player, String configName) {
        UUID playerId = player.getUniqueId();

        if (battleQueue.contains(playerId)) {
            player.sendMessage("§e你已经在匹配队列中！");
            return false;
        }

        if (cooldownPlayers.contains(playerId)) {
            player.sendMessage("§c你正处于冷却期，请稍后再试！");
            return false;
        }

        battleQueue.offer(playerId);
        Bukkit.broadcastMessage("§e" + player.getName() + "§a已加入匹配队列，当前人数: " + battleQueue.size());

        // 异步处理队列检查
        Bukkit.getScheduler().runTaskAsynchronously(Main.INSTANCE, () -> checkQueue(configName));
        return true;
    }

    private void checkQueue(String configName) {

        synchronized (battleQueue) {
            while (battleQueue.size() >= 2) {
                UUID player1Id = battleQueue.poll();
                UUID player2Id = battleQueue.poll();

                Player player1 = Bukkit.getPlayer(player1Id);
                Player player2 = Bukkit.getPlayer(player2Id);

                if (player1 == null || player2 == null) {
                    if (player1 != null) battleQueue.offer(player1Id);
                    if (player2 != null) battleQueue.offer(player2Id);
                    continue;
                }

                startBattleSession(player1, player2, configName);
            }
        }
    }

    private void startBattleSession(Player p1, Player p2, String configName) {
        addToBattle(p1.getUniqueId());
        addToBattle(p2.getUniqueId());

        p1.sendMessage("§b匹配成功！战斗将在3秒后开始...");
        p2.sendMessage("§b匹配成功！战斗将在3秒后开始...");


        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    EntityPlayerMP ep1 = PixelmonPlayerUtils.getUniquePlayerStartingWith(p1.getName());
                    EntityPlayerMP ep2 = PixelmonPlayerUtils.getUniquePlayerStartingWith(p2.getName());

                    BattleRules rules = createBattleRules(configName);
                    initiateBattle(ep1, ep2, rules);

                    applyCooldown(p1, p2);
                } catch (Exception e) {
                    handleBattleError(p1, p2, e);
                }
            }
        }.runTaskLater(Main.INSTANCE, QUEUE_DELAY_TICKS);
    }

    private BattleRules createBattleRules(String configName) {
        FileConfiguration config = getConfig("pvplist/" + configName);
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

    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }


    private void initiateBattle(EntityPlayerMP player1, EntityPlayerMP player2, BattleRules rules) {
        PlayerParticipant participant1 = new PlayerParticipant(player1);
        PlayerParticipant participant2 = new PlayerParticipant(player2);

        EntityPixelmon activePokemon1 = participant1.getStorage().getAndSendOutFirstAblePokemon(player1);
        EntityPixelmon activePokemon2 = participant2.getStorage().getAndSendOutFirstAblePokemon(player2);

        BattleQuery battleQuery = new BattleQuery(
                player1,
                activePokemon1,
                player2,
                activePokemon2
        );

        battleQuery.proposeRules(player1, rules);
        battleQuery.acceptQuery(player2, EnumBattleQueryResponse.Accept);
    }

    private void applyCooldown(Player... players) {
        for (Player p : players) {
            cooldownPlayers.add(p.getUniqueId());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : players) {
                    cooldownPlayers.remove(p.getUniqueId());
                }
            }
        }.runTaskLater(Main.INSTANCE, COOLDOWN_DURATION);
    }

    private void handleBattleError(Player p1, Player p2, Exception e) {
        p1.sendMessage("§c战斗初始化失败");
        p2.sendMessage("§c战斗初始化失败");
        Main.INSTANCE.getLogger().severe("战斗初始化错误: " + e.getMessage());
    }

    // 在战斗开始时记录玩家
    public static void addToBattle(UUID playerId) {
        activeBattles.put(playerId, true);
    }

    // 检查是否在战斗中
    public static boolean isInBattle(UUID playerId) {
        return activeBattles.containsKey(playerId);
    }

    // 战斗结束后移除
    public static void removeFromBattle(UUID playerId) {
        activeBattles.remove(playerId);
    }

    private static void addClausesToBattle(String path, BattleRules rules) {
        ArrayList<BattleClause> clauses = new ArrayList<>();
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.bagClause"))) {
            clauses.add(new BattleClause("bag"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.batonPassClause"))) {
            clauses.add(new MoveClause("batonpass", new String[]{"Baton Pass"}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.maxOneBatonPass"))) {
            clauses.add(new BattleClause("batonpass1"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.chatter"))) {
            clauses.add(new MoveClause("chatter", new String[]{"Chatter"}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.drizzle"))) {
            clauses.add(new AbilityClause("drizzle", new Class[]{Drizzle.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.drizzleSwim"))) {
            clauses.add(new AbilityComboClause("drizzleswim", new Class[]{Drizzle.class, SwiftSwim.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.drought"))) {
            clauses.add(new AbilityClause("drought", new Class[]{Drought.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.endlessBattle"))) {
            clauses.add(new BattleClauseSingleAll("endlessbattle", new BattleClause[]{new ItemPreventClause("", new EnumHeldItems[]{EnumHeldItems.leppa}), new MoveClause("", new String[]{"Recycle"}), new MoveClause("", new String[]{"Fling", "Heal Pulse", "Pain Split"})}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.evasionAbility"))) {
            clauses.add(new AbilityClause("evasionability", new Class[]{SandVeil.class, SnowCloak.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.evasion"))) {
            clauses.add(new MoveClause("evasion", new String[]{"Double Team", "Minimize"}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.forfeitClause"))) {
            clauses.add(new BattleClause("forfeit"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.inverseBattle"))) {
            clauses.add(new BattleClause("inverse"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.noDuplicateItems"))) {
            clauses.add(new BattleClause("item"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.legendBan"))) {
            clauses.add(new PokemonClause("legendary", EnumSpecies.LEGENDARY_ENUMS));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.megaStone"))) {
            clauses.add(new ItemPreventClause("mega", new EnumHeldItems[]{EnumHeldItems.megaStone}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.moody"))) {
            clauses.add(new AbilityClause("moody", new Class[]{Moody.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.ohKo"))) {
            clauses.add(new MoveClause("ohko", new String[]{"Fissure", "Guillotine", "Horn Drill", "Sheer Cold"}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.sandStream"))) {
            clauses.add(new AbilityClause("sandstream", new Class[]{SandStream.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.shadowTag"))) {
            clauses.add(new AbilityClause("shadowtag", new Class[]{ShadowTag.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.skyBattle"))) {
            clauses.add(new BattleClause("sky"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.sleepClause"))) {
            clauses.add(new BattleClause("sleep"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.smashPass"))) {
            clauses.add(new BattleClauseSingleAll("smashpass", new BattleClause[]{new MoveClause("batonpass", new String[]{"Baton Pass"}), new MoveClause("", new String[]{"Shell Smash"})}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.snowWarning"))) {
            clauses.add(new AbilityClause("snowwarning", new Class[]{SnowWarning.class}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.soulDew"))) {
            clauses.add(new BattleClauseSingleAll("souldew", new BattleClause[]{new PokemonClause("", new EnumSpecies[]{EnumSpecies.Latias, EnumSpecies.Latios}), new ItemPreventClause("", new EnumHeldItems[]{EnumHeldItems.soulDew})}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.multipleSpecies"))) {
            clauses.add(new BattleClause("pokemon"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.speedPass"))) {
            clauses.add(new BattleClause("speedpass"));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.swagger"))) {
            clauses.add(new MoveClause("swagger", new String[]{"Swagger"}));
        }
        if ("true".equalsIgnoreCase(getConfigFromPath(path).getString("Clauses.weatherSpeed"))) {
            clauses.add(new BattleClauseAny("weatherspeed", new BattleClause[]{new AbilityComboClause("drizzleswim", new Class[]{Drizzle.class, SwiftSwim.class}), new AbilityComboClause("", new Class[]{Drought.class, Chlorophyll.class}), new AbilityComboClause("", new Class[]{SandStream.class, SandRush.class})}));
        }
        rules.setNewClauses(clauses);
    }
    public static YamlConfiguration getConfigFromPath(String path) {

        File xxFile = new File(Main.INSTANCE.getDataFolder(), path + ".yml");

        if (!xxFile.exists()) {
            Main.INSTANCE.saveResource(path + ".yml", false); // 如果 xx.yml 存在于插件 jar 中，可以这样复制出来
        }

        return YamlConfiguration.loadConfiguration(xxFile);
    }



}