package com.langchao.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.langchao.ai.model.dto.message.MessageQueryRequest;
import com.langchao.ai.model.dto.message.MessageSendRequest;
import com.langchao.ai.model.entity.Message;
import com.langchao.ai.model.vo.MessageSendVO;
import com.langchao.ai.model.vo.MessageVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 20406
* @description 针对表【message(消息表)】的数据库操作Service
* @createDate 2024-07-17 14:15:22
*/
public interface MessageService extends IService<Message> {
    /**
     * 校验
     * @param message
     * @param add
     */
    void validMessage(Message message, boolean add);

    /**
     * 获取查询条件
     *
     * @param messageQueryRequest
     * @return
     */
    QueryWrapper<Message> getQueryWrapper(MessageQueryRequest messageQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param post
     * @param request
     * @return
     */
    MessageVO getMessageVO(Message post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param messagePage
     * @param request
     * @return
     */
    Page<MessageVO> getMessageVOPage(Page<Message> messagePage, HttpServletRequest request);

    /**
     * 发送消息
     *
     * @param messageService
     * @param request
     * @return
     */
    MessageSendVO sendMessage(MessageSendRequest messageSendRequest, HttpServletRequest request);
}
