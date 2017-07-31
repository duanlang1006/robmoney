package com.mit.state;

/**
 * Created by hxd on 16-3-7.
 */
public class Task {
    private ITaskStateListener stateListener;
    private String nextState;
    private String finishState;

    public Task(String nextState, String finishState) {
        this.nextState = nextState;
        this.finishState = finishState;
    }

    public String getNextState() {
        return nextState;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }

    public String getFinishState() {
        return finishState;
    }

    public ITaskStateListener getStateListener() {
        return stateListener;
    }

    public void setStateListener(ITaskStateListener stateListener) {
        this.stateListener = stateListener;
    }
}
