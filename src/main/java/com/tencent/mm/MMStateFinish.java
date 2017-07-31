package com.tencent.mm;

import android.content.Context;

import com.android.dsc.util.DscLog;
import com.mit.money.utils.PowerUtil;
import com.mit.state.ITaskState;
import com.mit.state.Task;

/**
 * Created by hxd on 16-3-8.
 */
public class MMStateFinish implements ITaskState {
    @Override
    public void process(Context context, Task task, Object... params) {
        MMTask mmTask = (MMTask) task;
        mmTask.setAutoInFlag(false);
        DscLog.d(this.getClass().getSimpleName(), "mmTask.getCount():" + mmTask.getCount());
        if (mmTask.getCount() == 0) {
            mmTask.setCount(mmTask.getCount() + 1);
            PowerUtil.getInstance(context).reenableKeyguard();
            if (null != task.getStateListener()) {
                task.getStateListener().onStateFinished(context, task);
            }
        }
    }
}
