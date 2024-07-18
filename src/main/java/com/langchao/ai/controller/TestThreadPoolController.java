package com.langchao.ai.controller;

import com.langchao.ai.common.BaseResponse;
import com.langchao.ai.common.ResultUtils;
import com.langchao.ai.config.CallRunable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 测试线程池
 */
@RestController
@RequestMapping("/testthreadpool")
@Slf4j
public class TestThreadPoolController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 测试线程池
     *
     * @return
     */
    @GetMapping("/test")
    public BaseResponse<Boolean> testThreadPool() {
        CallRunable callRunable = new CallRunable(new Runnable() {
            @Override
            public void run() {
                log.info("线程池启动！");
                try {
                    Thread.sleep(100000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 1L, 1L, "");

        CompletableFuture.runAsync(callRunable, threadPoolExecutor);

        return ResultUtils.success(true);
    }
}
