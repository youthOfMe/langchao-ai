# 数据库初始化

-- 创建库
create database if not exists langchao_ai;

-- 切换库
use langchao_ai;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 窗口表
create table if not exists chat_windows (
    id           bigint auto_increment comment 'id' primary key,
    userId  bigint                           not null comment '用户ID',
    `type`  tinyint                          not null comment '窗口类型 1 = 紧急政务窗口 0 = 普通窗口',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '窗口表' collate = utf8mb4_unicode_ci;

-- 消息表 后期肯定是要进行垂直分库的
create table if not exists message
(
    id           bigint auto_increment comment 'id' primary key,
    userId  bigint                           not null comment '用户ID',
    chatWindowId bigint                      not null comment '窗口ID',
    `type`  tinyint                          not null comment '消息类型 0 = 用户信息 1 = AI推送信息 2 = 警告信息 3 = 提示信息 4 = 推荐信息',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_chatWindowId (chatWindowId)
) comment '消息表' collate = utf8mb4_unicode_ci;

-- 拒绝任务表
create table if not exists reject_task
(
    id           bigint auto_increment comment 'id' primary key,
    userId  bigint                           not null comment '用户ID',
    chatWindowId bigint                      not null comment '窗口ID',
    task text                      not null comment '窗口ID',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
) comment '消息表' collate = utf8mb4_unicode_ci;

-- 模型表
create table if not exists model
(
    id           bigint auto_increment comment 'id' primary key,
    userId  bigint                           not null comment '用户ID',
    `type`  tinyint                          not null comment '模型类型 0 = 官方模型 1 = 民间模型',
    name varchar(128)                       not null comment '模型名称',
    desc varchar(512)                       not null comment '模型描述',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
) comment '窗口表' collate = utf8mb4_unicode_ci;