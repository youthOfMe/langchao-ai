package com.langchao.ai.model.dto.chatwindows;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 *

 */
@Data
public class ChatWindowsEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 窗口类型 1 = 紧急政务窗口 0 = 普通窗口
     */
    private Integer type;


    private static final long serialVersionUID = 1L;
}