package org.spoofax.jsglr2.parsetable;

import org.spoofax.jsglr2.states.IState;

public class ParseTable implements IParseTable {

    final private IState[] states;
    final private int startStateId;

    public ParseTable(IState[] states, int startStateId) {
        this.states = states;
        this.startStateId = startStateId;
    }

    @Override
    public IState getStartState() {
        return getState(startStateId);
    }

    @Override
    public IState getState(int stateId) {
        return states[stateId];
    }

}
