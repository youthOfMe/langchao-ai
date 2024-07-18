package com.langchao.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.langchao.ai.model.dto.message.MessageQueryRequest;
import com.langchao.ai.model.dto.message.MessageSendRequest;
import com.langchao.ai.model.entity.Message;
import com.langchao.ai.model.vo.MessageSendVO;
import com.langchao.ai.model.vo.MessageVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

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
     * @param messageSendRequest
     * @param request
     * @return
     */
    MessageSendVO sendMessage(MessageSendRequest messageSendRequest, HttpServletRequest request);


    /**
     * 发送消息（异步 + SSE）
     *
     * @param messageSendRequest
     * @param request
     * @return
     */
    SseEmitter sendMessageAsync(MessageSendRequest messageSendRequest, HttpServletRequest request) throws IOException;

    /**
     * 获取消息列表
     *
     * @param chatWindowsId
     * @param request
     * @return
     */
    List<Message> listMessage(Long chatWindowsId, HttpServletRequest request);
}
