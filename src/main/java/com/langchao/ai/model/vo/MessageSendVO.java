package com.langchao.ai.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送消息后的响应信息
 */
@Data
public class MessageSendVO implements Serializable {

    private String userMessage;

    private String aiMessage;
}
