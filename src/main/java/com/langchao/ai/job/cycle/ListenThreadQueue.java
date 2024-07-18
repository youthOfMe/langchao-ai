package com.langchao.ai.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langchao.ai.model.entity.ChatWindows;
import com.langchao.ai.model.entity.RejectTask;
import com.langchao.ai.service.ChatWindowsService;
import com.langchao.ai.service.RejectTaskService;
import com.langchao.ai.utils.DoAsyncAI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class ListenThreadQueue {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private DoAsyncAI doAsyncAI;

    @Resource
    private RejectTaskService rejectTaskService;

    @Resource
    private ChatWindowsService chatWindowsService;

    /**
     * 每分钟执行一次
     */
    // @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // todo 最好还是加把锁
        // 监听队列是否为空
        if (threadPoolExecutor.getTaskCount() < 2 || threadPoolExecutor.getTaskCount() > 3) {
            return;
        }
        // 先查出任务
        QueryWrapper<RejectTask> queryWrapper = new QueryWrapper<>();
        // 指定按照某个日期字段升序排列
        queryWrapper.orderByAsc("createTime");
        // 查询第一条记录
        Page<RejectTask> page = new Page<>(1, 1); // 第一个参数为当前页码，第二个参数为每页显示的记录数
        RejectTask rejectTask = rejectTaskService.page(page, queryWrapper).getRecords().get(0);
        if (rejectTask == null) {
            // 没查出来 就走吧
            return;
        }
        // 查询出chatWindows
        ChatWindows chatWindows = chatWindowsService.getById(rejectTask.getChatWindowId());
        // 将任务添加到队列
        threadPoolExecutor.execute(() -> doAsyncAI.asyncUserAI(rejectTask.getUserId(), rejectTask.getChatWindowId(), null, chatWindows, rejectTask, rejectTask.getTask()));
    }
}
