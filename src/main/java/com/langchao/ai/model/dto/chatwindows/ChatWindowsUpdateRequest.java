package com.langchao.ai.model.dto.chatwindows;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新请求
 *

 */
@Data
public class ChatWindowsUpdateRequest implements Serializable {

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}