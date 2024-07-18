package com.langchao.ai.config;

import lombok.Data;

import java.io.Serializable;

@Data
public class CallRunable implements Runnable, Serializable {

    private Runnable task;
    private String content;
    private Long chatWindowsId;
    private Long userId;

    public CallRunable(Runnable task, Long chatWindowsId, Long userId, String content) {
        this.task = task;
        this.chatWindowsId = chatWindowsId;
        this.userId = userId;
        this.content = content;
    }

    @Override
    public void run() {
        task.run();
    }

}
