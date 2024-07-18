package com.langchao.ai.job.cycle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 作者：洋哥
 * 描述：牛逼
 */
@SpringBootTest
class ListenThreadQueueTest {

    @Resource
    private ListenThreadQueue listenThreadQueue;

    @Test
    void test() {
        listenThreadQueue.run();
    }
}