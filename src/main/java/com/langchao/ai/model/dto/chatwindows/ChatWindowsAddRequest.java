package com.langchao.ai.model.dto.chatwindows;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *

 */
@Data
public class ChatWindowsAddRequest implements Serializable {


    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 窗口类型 1 = 紧急政务窗口 0 = 普通窗口
     */
    private Integer type;

    /**
     * 会话标题
     */
    private String title;

    private static final long serialVersionUID = 1L;
}