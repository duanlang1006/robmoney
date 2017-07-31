package com.mit.state;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.android.dsc.util.DscLog;

import java.util.HashMap;

/**
 * Created by hxd on 15-12-12.
 */
public class StateMachine {
    private static HashMap<String, ITaskState> instanceMap = new HashMap<>();
    private static Handler sHandler;
    private static final HandlerThread sThread = new HandlerThread("StateMachine");
    private static TimeoutRunnable timeout;

    static {
        sThread.start();
        DscLog.setIsDebug(false);
    }

    public static void executeState(final Context context, final Task task, final Object... params) {
        if (null == task || TextUtils.isEmpty(task.getNextState())) {
            return;
        }
        if (null == sHandler) {
            sHandler = new Handler(/*sThread.getLooper()*/);
        }
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (null == task.getNextState()) {
                        return;
                    }
                    ITaskState state = getJobState(task.getNextState());
                    state.process(context, task, params);

                    if (null == timeout) {
                        timeout = new TimeoutRunnable(context, task);
                    } else if (timeout.getTask() != task) {
                        timeout.setTask(task);
                    }
                    sHandler.removeCallbacks(timeout);
                    sHandler.postDelayed(timeout, 10000);
                } catch (Exception e) {
                    e.printStackTrace();
                    task.setNextState(task.getFinishState());
                    ITaskState state = getJobState(task.getNextState());
                    state.process(context, task, params);

                }
                DscLog.d("TaskStateMachine", "executeState:task:" + task.getClass().getName() + ",state:" + task.getNextState());
            }
        });
    }


    public static ITaskState getJobState(String handler) {
        ITaskState state = null;
        try {
            if (instanceMap.containsKey(handler)) {
                state = instanceMap.get(handler);
            } else {
                Class cls = Class.forName(handler);
                state = (ITaskState) cls.newInstance();
                instanceMap.put(handler, state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    public static void clear() {
        if (null != sHandler) {
            sHandler.removeCallbacks(timeout);
        }
    }

    static class TimeoutRunnable implements Runnable {
        private Context context;
        private Task task;

        public TimeoutRunnable(Context context, Task task) {
            this.context = context;
            this.task = task;
        }

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (null != task) {
                task.setNextState(task.getFinishState());
                ITaskState state = getJobState(task.getNextState());
                state.process(context, task);
                DscLog.d("TaskStateMachine", "timeout:task:" + task.getClass().getName() + ",state:" + task.getNextState());
            }
        }
    }
}
