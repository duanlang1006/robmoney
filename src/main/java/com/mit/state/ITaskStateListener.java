package com.mit.state;

import android.content.Context;

/**
 * Created by hxd on 15-12-9.
 */
public interface ITaskStateListener {
    public void onStateStarted(Context context, Task task);
    public void onStateFinished(Context context, Task task);
}