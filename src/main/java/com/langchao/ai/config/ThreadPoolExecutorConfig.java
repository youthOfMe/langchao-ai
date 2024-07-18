package com.langchao.ai.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Resource
    private ThreadPoolRejectionHandler threadPoolRejectionHandler;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {

        // 配置线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程" + count++);
                return thread;
            }
        };

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2, 100, TimeUnit.SECONDS,
                 new ArrayBlockingQueue<>(2), threadFactory, threadPoolRejectionHandler);
        return threadPoolExecutor;
    }
}
