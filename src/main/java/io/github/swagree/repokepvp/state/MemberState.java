package io.github.swagree.repokepvp.state;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import io.github.swagree.repokepvp.entity.Member;

public interface MemberState {
    void handleJoinQueue(String configName);
    void handleMatchFound(Member opponent);
    void handleStartBattle(BattleRules rules);
    void handleEndBattle();
}