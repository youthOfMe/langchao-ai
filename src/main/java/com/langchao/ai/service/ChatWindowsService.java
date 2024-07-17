package com.langchao.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.langchao.ai.model.dto.chatwindows.ChatWindowsQueryRequest;
import com.langchao.ai.model.entity.ChatWindows;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.model.vo.ChatWindowsVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 20406
* @description 针对表【chat_windows(窗口表)】的数据库操作Service
* @createDate 2024-07-17 12:40:43
*/
public interface ChatWindowsService extends IService<ChatWindows> {

    /**
     * 校验
     * @param chatWindows
     * @param add
     */
    void validChatWindows(ChatWindows chatWindows, boolean add);

    /**
     * 获取查询条件
     *
     * @param chatWindowsQueryRequest
     * @return
     */
    QueryWrapper<ChatWindows> getQueryWrapper(ChatWindowsQueryRequest chatWindowsQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param post
     * @param request
     * @return
     */
    ChatWindowsVO getChatWindowsVO(ChatWindows post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param chatWindowsPage
     * @param request
     * @return
     */
    Page<ChatWindowsVO> getChatWindowsVOPage(Page<ChatWindows> chatWindowsPage, HttpServletRequest request);

    /**
     * 用户创建会话
     *
     * @param type
     * @param loginUser
     * @return
     */
    Boolean createChatWindows(Integer type, User loginUser);
}
