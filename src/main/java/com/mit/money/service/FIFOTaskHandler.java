package com.mit.money.service;

import android.content.Context;
import android.util.Pair;
import android.view.accessibility.AccessibilityEvent;

import com.mit.state.ITaskStateListener;
import com.mit.state.StateMachine;
import com.mit.state.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hxd on 16-3-8.
 */
public class FIFOTaskHandler implements ITaskStateListener {
    private static FIFOTaskHandler instance = null;

    public static FIFOTaskHandler getInstance() {
        if (null == instance) {
            instance = new FIFOTaskHandler();
        }
        return instance;
    }

    private FIFOTaskHandler() {
        mTaskList = Collections.synchronizedList(new ArrayList());
    }

    private List<Pair<Task, AccessibilityEvent>> mTaskList;
    private Task mTask;

    public void onInit() {
        mTask = null;
        mTaskList.clear();
    }

    public void onEnque(Context context, Task task, AccessibilityEvent event) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mTaskList.add(new Pair<Task, AccessibilityEvent>(task, AccessibilityEvent.obtain(event)));
            if (null == mTask) {
                Pair<Task, AccessibilityEvent> pair = mTaskList.remove(0);
                mTask = pair.first;
                AccessibilityEvent param = pair.second;
                StateMachine.executeState(context, mTask, param);
            }
        }
    }

    public void onEvent(Context context, AccessibilityEvent event) {
        if (null != mTask
                && mTask.getClass().getPackage().getName().equals(event.getPackageName().toString())) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                StateMachine.executeState(context, mTask, AccessibilityEvent.obtain(event));
            }
        }
    }

    @Override
    public void onStateStarted(Context context, Task task) {

    }

    @Override
    public void onStateFinished(Context context, Task stateInfo) {
        if (mTask != null) {
            mTask.setNextState(null);
            mTask = null;
        }
        StateMachine.clear();
        if (mTaskList.size() > 0) {
            Pair<Task, AccessibilityEvent> pair = mTaskList.remove(0);
            mTask = pair.first;
            AccessibilityEvent param = pair.second;
            StateMachine.executeState(context, mTask, param);
        }
    }
}
