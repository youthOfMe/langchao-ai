package com.langchao.ai.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 *

 */
@Data
public class MessageEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 窗口ID
     */
    private Long chatWindowId;

    /**
     * 消息类型 0 = 用户信息 1 = AI推送信息 2 = 警告信息 3 = 提示信息 4 = 推荐信息
     */
    private Integer type;

    /**
     * 消息内容
     */
    private String content;


    private static final long serialVersionUID = 1L;
}