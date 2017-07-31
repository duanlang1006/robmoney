package com.mit.money.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SimpleBundle implements BundleActivator {
    public void start(BundleContext context) throws Exception {
//        System.err.println("你好我是插件,我将为你展示启动acitivty我已经启动了 我的BundleId为："+context.getBundle().getBundleId());
        //将BundleContext放入BundleContextFactory工厂
        //方便插件的其他类里面调用
        BundleContextFactory.getInstance().setBundleContext(context);
    }

    public void stop(BundleContext context) {
//        System.err.println("你好我是插件,我被停止�?我的BundleId为："+context.getBundle().getBundleId());
    }

}
