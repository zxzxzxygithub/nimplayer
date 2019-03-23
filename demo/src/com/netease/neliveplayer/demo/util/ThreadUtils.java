package com.netease.neliveplayer.demo.util;

/**
 * Created by weilv on 2018/4/17.
 */

public class ThreadUtils {
    public static String getThreadInfo() {
        return " @[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId()
                + "]";
    }
}
