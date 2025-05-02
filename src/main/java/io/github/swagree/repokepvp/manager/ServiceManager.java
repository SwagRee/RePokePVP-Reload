package io.github.swagree.repokepvp.manager;

import io.github.swagree.Main;
import io.github.swagree.repokepvp.manager.dataManager.BattleManager;
import io.github.swagree.repokepvp.manager.matchManager.*;
import io.github.swagree.repokepvp.manager.memberManager.MemberManager;
import io.github.swagree.repokepvp.manager.ruleManager.RuleManager;

public class ServiceManager {

    private final BattleManager battleManager;
    private final RuleManager ruleManager;
    private final MatchQueueManager matchQueueManager;
    private final BattleStateManager battleStateManager;
    private final BattleValidator battleValidator;
    private final BattleStarter battleStarter;
    private final MemberManager memberManager;

    public ServiceManager(Main plugin) {
        // 初始化所有管理器
        this.battleManager = new BattleManager(plugin);
        this.ruleManager = new RuleManager();
        this.matchQueueManager = new MatchQueueManager();
        this.battleStateManager = new BattleStateManager();
        this.battleValidator = new BattleValidator();
        this.battleStarter = new BattleStarter();
        this.memberManager = new MemberManager(this);
    }

    public void shutdown() {
        if (battleManager != null) {
            battleManager.close();
        }
    }

    // Getter 方法
    public BattleManager getBattleManager() {
        return battleManager;
    }

    public RuleManager getRuleManager() {
        return ruleManager;
    }

    public MatchQueueManager getMatchQueueManager() {
        return matchQueueManager;
    }

    public BattleStateManager getBattleStateManager() {
        return battleStateManager;
    }

    public BattleValidator getBattleValidator() {
        return battleValidator;
    }

    public BattleStarter getBattleStarter() {
        return battleStarter;
    }

    public MemberManager getMemberManager() {
        return memberManager;
    }
}