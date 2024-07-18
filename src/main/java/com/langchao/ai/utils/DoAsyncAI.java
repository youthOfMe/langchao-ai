package com.langchao.ai.utils;

import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.exception.ThrowUtils;
import com.langchao.ai.manager.AiManager;
import com.langchao.ai.mapper.RejectTaskMapper;
import com.langchao.ai.model.entity.ChatWindows;
import com.langchao.ai.model.entity.Message;
import com.langchao.ai.model.entity.RejectTask;
import com.langchao.ai.model.enums.MessageEnum;
import com.langchao.ai.service.ChatWindowsService;
import com.langchao.ai.service.MessageService;
import com.langchao.ai.service.RejectTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class DoAsyncAI {

    @Resource
    private AiManager aiManager;

    @Resource
    private RejectTaskService rejectTaskService;

    @Resource
    private RejectTaskMapper rejectTaskMapper;

    @Resource
    private ChatWindowsService chatWindowsService;

    @Resource
    private MessageService messageService;

    public void asyncUserAI(long userId, long chatWindowId, SseEmitter sseEmitter, ChatWindows chatWindows, RejectTask rejectTask, String content) {
        // todo 发送消息给AI
        long aiModeId = 1813472675464413185L;
        // 调用 AI
        // String result = aiManager.doChat(aiModeId, content);
        String result = "AI响应成功！";
        // 存储AI信息调用信息
        Message aiMessage = new Message();
        aiMessage.setUserId(userId);
        aiMessage.setChatWindowId(chatWindowId);
        aiMessage.setType(MessageEnum.AI.getValue());
        aiMessage.setContent(result);
        boolean saveAiMes = messageService.save(aiMessage);
        ThrowUtils.throwIf(!saveAiMes, ErrorCode.SYSTEM_ERROR, "AI调用失败！");

        try {
            if (sseEmitter != null) {
                // 释放消息
                sseEmitter.send(result);
            }
            // 解锁对话
            chatWindows.setCanSend(0);
            chatWindowsService.updateById(chatWindows);
        } catch (IOException e) {
            log.error("消息推送失败: {}", e);
        } finally {
            if (sseEmitter != null) {
                sseEmitter.complete();
            }
        }

        // 判断是否要继续通知用户任务执行完毕
        RejectTask task = rejectTaskService.getById(rejectTask.getId());
        if (task.getIsNotify() == 0) {
            // todo 短信服务通知
        }
        rejectTaskMapper.deleteById(task);
    }
}
