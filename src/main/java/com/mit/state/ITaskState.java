package com.mit.state;

import android.content.Context;

/**
 * Created by hxd on 15-12-9.
 */
public interface ITaskState {
    public void process(Context context, Task task, Object... params);
}