package com.example.myapplication.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static AppExecutors instance;
    private final ExecutorService networkExecutor;
    private final ExecutorService diskExecutor;

    private AppExecutors() {
        // 网络请求线程池，限制并发数为3
        networkExecutor = Executors.newFixedThreadPool(3);
        // 磁盘操作线程池
        diskExecutor = Executors.newSingleThreadExecutor();
    }

    public static synchronized AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    public ExecutorService getNetworkExecutor() {
        return networkExecutor;
    }

    public ExecutorService getDiskExecutor() {
        return diskExecutor;
    }

    public void shutdown() {
        networkExecutor.shutdown();
        diskExecutor.shutdown();
    }
}