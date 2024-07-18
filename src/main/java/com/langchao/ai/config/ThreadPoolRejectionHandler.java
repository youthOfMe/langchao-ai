package com.langchao.ai.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langchao.ai.model.entity.RejectTask;
import com.langchao.ai.service.RejectTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池拒绝策略
 */
@Slf4j
@Component
public class ThreadPoolRejectionHandler implements RejectedExecutionHandler {

    @Resource
    private RejectTaskService rejectTaskService;

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (r instanceof CallRunable) {
            CallRunable customRunnable = (CallRunable) r;
            Long chatWindowsId = customRunnable.getChatWindowsId();
            Long userId = customRunnable.getUserId();
            String content = customRunnable.getContent();
            // 进行执行持久化任务
            QueryWrapper<RejectTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userId);
            queryWrapper.eq("chatWindowId", chatWindowsId);
            RejectTask task = rejectTaskService.getOne(queryWrapper);
            // 更新任务
            task.setTask(content);
            rejectTaskService.updateById(task);
        } else {
            System.out.println("Task was rejected, but it's not a CallRunnable instance.");
        }


        System.out.println("失败啦");
    }
}
