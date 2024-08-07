package com.langchao.ai.model.dto.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 预设消息DTO
 */
@Data
public class PreMessageDTO implements Serializable {

    /**
     * 角色
     */
    private String role;

    /**
     * 消息
     */
    private String message;

    private static final long serialVersionUID = 1L;
}
