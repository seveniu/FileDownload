package com.seveniu.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: seveniu
 * Date: 8/20/15
 * Time: 6:08 PM
 * Project: qiniu
 */
public class ShutdownHookManager {
    private static ShutdownHookManager instance = new ShutdownHookManager();
    private ConcurrentHashMap<Integer, ShutdownHook> map = new ConcurrentHashMap<>();


    public static ShutdownHookManager get() {
        return instance;
    }


    public void register(ShutdownHook shutdownHook) {
        map.put(shutdownHook.hashCode(), shutdownHook);
    }


    private ShutdownHookManager() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                map.values().forEach(ShutdownHook::shutdown);
            }
        });
    }
}
