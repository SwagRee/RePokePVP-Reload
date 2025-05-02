package io.github.swagree.repokepvp.state.impl;

import io.github.swagree.repokepvp.entity.Member;
import io.github.swagree.repokepvp.manager.ServiceManager;
import io.github.swagree.repokepvp.state.MemberState;

public abstract class BaseState implements MemberState {
    protected final Member member;
    protected final ServiceManager serviceManager;

    public BaseState(Member member, ServiceManager serviceManager) {
        this.member = member;
        this.serviceManager = serviceManager;
    }
}