package com.langchao.ai.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void testAi() {
        aiManager.doChat(1659171950288818178L, "hhh");
    }
}