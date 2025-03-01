package com.langchao.ai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 窗口表
 * @TableName model
 */
@TableName(value ="model")
@Data
public class Model implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
    private String description;

    /**
     * 对话示例
     */
    private String conversation;

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
    private String preMessage;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}