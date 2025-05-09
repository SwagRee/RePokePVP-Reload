package io.github.swagree.repokepvp.manager;

import io.github.swagree.repokepvp.Main;
import io.github.swagree.repokepvp.manager.dataManager.BattleResultManager;
import io.github.swagree.repokepvp.manager.matchManager.*;
import io.github.swagree.repokepvp.manager.memberManager.MemberManager;
import io.github.swagree.repokepvp.manager.ruleManager.RuleManager;

public class ServiceManager {

    private final BattleResultManager battleManager;
    private final RuleManager ruleManager;
    private final MatchQueueManager matchQueueManager;
    private final BattleStateManager battleStateManager;
    private final BattleValidator battleValidator;
    private final BattleStarter battleStarter;
    private final MemberManager memberManager;

    public ServiceManager(Main plugin) {
        // 初始化所有管理器
        this.battleManager = new BattleResultManager(plugin);
        this.ruleManager = new RuleManager();
        this.matchQueueManager = new MatchQueueManager(plugin,this);
        this.battleStateManager = new BattleStateManager();
        this.battleValidator = new BattleValidator();
        this.battleStarter = new BattleStarter();
        this.memberManager = new MemberManager(this);
    }

    public void shutdown() {
        battleManager.close();
    }

    // Getter 方法
    public BattleResultManager getBattleManager() {
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