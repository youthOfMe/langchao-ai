package com.langchao.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
@Slf4j
class ThreadPoolExecutorConfigTest {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Test
    void testThreadPool() throws InterruptedException {

        for (int i = 0; i < 9; i++) {
            CallRunable callRunable = new CallRunable(new Runnable() {
                @Override
                public void run() {
                    log.info("线程池启动！");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 1L, 1L, "555");
            threadPoolExecutor.execute(callRunable);
        }
        Thread.sleep(10000);
        for (int i = 0; i < 3; i++) {
            CallRunable callRunable = new CallRunable(new Runnable() {
                @Override
                public void run() {
                    log.info("线程池启动！");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 1L, 1L, "555");
            threadPoolExecutor.execute(callRunable);
        }
    }
}