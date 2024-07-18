package com.langchao.ai.config;

import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.Serializable;

@Data
public class CallRunable implements Runnable, Serializable {

    private Runnable task;
    private String content;
    private Long chatWindowsId;
    private Long userId;
    private SseEmitter sseEmitter;

    public CallRunable(Runnable task, Long chatWindowsId, Long userId, String content) {
        this.task = task;
        this.chatWindowsId = chatWindowsId;
        this.userId = userId;
        this.content = content;
    }

    public CallRunable(Runnable task, Long chatWindowsId, Long userId, String content, SseEmitter sseEmitter) {
        this.task = task;
        this.chatWindowsId = chatWindowsId;
        this.userId = userId;
        this.content = content;
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void run() {
        task.run();
    }

}
