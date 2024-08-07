package com.langchao.ai.model.dto.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 窗口表
 * @TableName model
 */
@Data
public class ModelAddRequest implements Serializable {

    /**
     * 模型类型 0 = 官方模型 1 = 民间模型
     */
    private Integer type;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型的设定，SystemPrompt
     */
    private String systemPrompt;

    /**
     * 模型封面
     */
    private String coverUrl;

    /**
     * 介绍
     */
    private String introduce;

    /**
     * 模型描述
     */
    private String c;

    /**
     * 对话示例
     */
    private List<String> conversation;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 是否可见 0 = 可见 1 = 不可见
     */
    private Integer visable;

    /**
     * 预设消息
     */
    private List<PreMessageDTO> preMessage;

    /**
     * 关联消息数
     */
    private Integer linkedMessage;

    /**
     * 回复随机性
     */
    private Integer random;

    /**
     * 回复新鲜度
     */
    private Integer fresh;

    /**
     * 重复词惩罚
     */
    private Integer repeatNum;

    private static final long serialVersionUID = 1L;
}