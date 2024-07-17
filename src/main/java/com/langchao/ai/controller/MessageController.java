package com.langchao.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langchao.ai.annotation.AuthCheck;
import com.langchao.ai.common.BaseResponse;
import com.langchao.ai.common.DeleteRequest;
import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.common.ResultUtils;
import com.langchao.ai.constant.UserConstant;
import com.langchao.ai.exception.BusinessException;
import com.langchao.ai.exception.ThrowUtils;
import com.langchao.ai.model.dto.message.*;
import com.langchao.ai.model.entity.Message;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.model.vo.MessageVO;
import com.langchao.ai.service.MessageService;
import com.langchao.ai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 消息
 *
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    /**
     * 发送消息
     *
     * @param messageSendRequest
     * @return
     */
    @PostMapping("/sendMessage")
    public BaseResponse<Boolean> SendMessage(@RequestBody MessageSendRequest messageSendRequest, HttpServletRequest request) {
        Boolean isSusscess = messageService.sendMessage(messageSendRequest, request);
        return ResultUtils.success(isSusscess);
    }

    // public BaseResponse<>



    // region 增删改查

    /**
     * 创建
     *
     * @param messageAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addMessage(@RequestBody MessageAddRequest messageAddRequest, HttpServletRequest request) {
        if (messageAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Message message = new Message();
        BeanUtils.copyProperties(messageAddRequest, message);
        messageService.validMessage(message, true);
        message.setUserId(messageAddRequest.getUserId());
        boolean result = messageService.save(message);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newMessageId = message.getId();
        return ResultUtils.success(newMessageId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMessage(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Message oldMessage = messageService.getById(id);
        ThrowUtils.throwIf(oldMessage == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldMessage.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = messageService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param messageUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMessage(@RequestBody MessageUpdateRequest messageUpdateRequest, HttpServletRequest request) {
        if (messageUpdateRequest == null || messageUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断用户是否登录
        userService.isLoginUser(request);

        Message message = new Message();
        BeanUtils.copyProperties(messageUpdateRequest, message);
        // 参数校验
        messageService.validMessage(message, false);
        long id = messageUpdateRequest.getId();
        // 判断是否存在
        Message oldMessage = messageService.getById(id);
        ThrowUtils.throwIf(oldMessage == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = messageService.updateById(message);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<MessageVO> getMessageVOById(long id, HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Message message = messageService.getById(id);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(messageService.getMessageVO(message, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param messageQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Message>> listMessageByPage(@RequestBody MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        Page<Message> messagePage = messageService.page(new Page<>(current, size),
                messageService.getQueryWrapper(messageQueryRequest));
        return ResultUtils.success(messagePage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param messageQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<MessageVO>> listMessageVOByPage(@RequestBody MessageQueryRequest messageQueryRequest,
            HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Message> messagePage = messageService.page(new Page<>(current, size),
                messageService.getQueryWrapper(messageQueryRequest));
        return ResultUtils.success(messageService.getMessageVOPage(messagePage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param messageQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<MessageVO>> listMyMessageVOByPage(@RequestBody MessageQueryRequest messageQueryRequest,
            HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        if (messageQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        messageQueryRequest.setUserId(loginUser.getId());
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Message> messagePage = messageService.page(new Page<>(current, size),
                messageService.getQueryWrapper(messageQueryRequest));
        return ResultUtils.success(messageService.getMessageVOPage(messagePage, request));
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param messageEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editMessage(@RequestBody MessageEditRequest messageEditRequest, HttpServletRequest request) {
        if (messageEditRequest == null || messageEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断用户是否登录
        User loginUser = userService.getLoginUser(request);
        Message message = new Message();
        BeanUtils.copyProperties(messageEditRequest, message);
        // 参数校验
        messageService.validMessage(message, false);
        long id = messageEditRequest.getId();
        // 判断是否存在
        Message oldMessage = messageService.getById(id);
        ThrowUtils.throwIf(oldMessage == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldMessage.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = messageService.updateById(message);
        return ResultUtils.success(result);
    }

}
