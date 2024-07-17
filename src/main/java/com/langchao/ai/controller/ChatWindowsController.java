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
import com.langchao.ai.model.dto.chatwindows.ChatWindowsAddRequest;
import com.langchao.ai.model.dto.chatwindows.ChatWindowsEditRequest;
import com.langchao.ai.model.dto.chatwindows.ChatWindowsQueryRequest;
import com.langchao.ai.model.dto.chatwindows.ChatWindowsUpdateRequest;
import com.langchao.ai.model.entity.ChatWindows;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.model.enums.ChatWindowsEnum;
import com.langchao.ai.model.vo.ChatWindowsVO;
import com.langchao.ai.service.ChatWindowsService;
import com.langchao.ai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 聊天窗口接口
 *
 */
@RestController
@RequestMapping("/chatWindows")
@Slf4j
public class ChatWindowsController {

    @Resource
    private ChatWindowsService chatWindowsService;

    @Resource
    private UserService userService;

    /**
     * 创建会话
     *
     * @param type
     * @param request
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<Boolean> createChatWindows(@RequestParam("type") Integer type, HttpServletRequest request) {
        // 校验参数
        ChatWindowsEnum enumByValue = ChatWindowsEnum.getEnumByValue(type);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "类型参数错误！");
        // 判断用户是否登录
        User loginUser = userService.getLoginUser(request);
        Boolean isSuccess = chatWindowsService.createChatWindows(type, loginUser);
        return ResultUtils.success(isSuccess);
    }

    // public BaseResponse<>



    // region 增删改查

    /**
     * 创建
     *
     * @param chatWindowsAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChatWindows(@RequestBody ChatWindowsAddRequest chatWindowsAddRequest, HttpServletRequest request) {
        if (chatWindowsAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        ChatWindows chatWindows = new ChatWindows();
        BeanUtils.copyProperties(chatWindowsAddRequest, chatWindows);
        chatWindowsService.validChatWindows(chatWindows, true);
        chatWindows.setUserId(chatWindowsAddRequest.getUserId());
        boolean result = chatWindowsService.save(chatWindows);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChatWindowsId = chatWindows.getId();
        return ResultUtils.success(newChatWindowsId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChatWindows(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ChatWindows oldChatWindows = chatWindowsService.getById(id);
        ThrowUtils.throwIf(oldChatWindows == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChatWindows.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chatWindowsService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chatWindowsUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChatWindows(@RequestBody ChatWindowsUpdateRequest chatWindowsUpdateRequest, HttpServletRequest request) {
        if (chatWindowsUpdateRequest == null || chatWindowsUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断用户是否登录
        userService.isLoginUser(request);

        ChatWindows chatWindows = new ChatWindows();
        BeanUtils.copyProperties(chatWindowsUpdateRequest, chatWindows);
        // 参数校验
        chatWindowsService.validChatWindows(chatWindows, false);
        long id = chatWindowsUpdateRequest.getId();
        // 判断是否存在
        ChatWindows oldChatWindows = chatWindowsService.getById(id);
        ThrowUtils.throwIf(oldChatWindows == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chatWindowsService.updateById(chatWindows);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChatWindowsVO> getChatWindowsVOById(long id, HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ChatWindows chatWindows = chatWindowsService.getById(id);
        if (chatWindows == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chatWindowsService.getChatWindowsVO(chatWindows, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chatWindowsQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatWindows>> listChatWindowsByPage(@RequestBody ChatWindowsQueryRequest chatWindowsQueryRequest, HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        long current = chatWindowsQueryRequest.getCurrent();
        long size = chatWindowsQueryRequest.getPageSize();
        Page<ChatWindows> chatWindowsPage = chatWindowsService.page(new Page<>(current, size),
                chatWindowsService.getQueryWrapper(chatWindowsQueryRequest));
        return ResultUtils.success(chatWindowsPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chatWindowsQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ChatWindowsVO>> listChatWindowsVOByPage(@RequestBody ChatWindowsQueryRequest chatWindowsQueryRequest,
            HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        long current = chatWindowsQueryRequest.getCurrent();
        long size = chatWindowsQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ChatWindows> chatWindowsPage = chatWindowsService.page(new Page<>(current, size),
                chatWindowsService.getQueryWrapper(chatWindowsQueryRequest));
        return ResultUtils.success(chatWindowsService.getChatWindowsVOPage(chatWindowsPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chatWindowsQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ChatWindowsVO>> listMyChatWindowsVOByPage(@RequestBody ChatWindowsQueryRequest chatWindowsQueryRequest,
            HttpServletRequest request) {
        // 判断用户是否登录
        userService.isLoginUser(request);
        if (chatWindowsQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chatWindowsQueryRequest.setUserId(loginUser.getId());
        long current = chatWindowsQueryRequest.getCurrent();
        long size = chatWindowsQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ChatWindows> chatWindowsPage = chatWindowsService.page(new Page<>(current, size),
                chatWindowsService.getQueryWrapper(chatWindowsQueryRequest));
        return ResultUtils.success(chatWindowsService.getChatWindowsVOPage(chatWindowsPage, request));
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param chatWindowsEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChatWindows(@RequestBody ChatWindowsEditRequest chatWindowsEditRequest, HttpServletRequest request) {
        if (chatWindowsEditRequest == null || chatWindowsEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断用户是否登录
        User loginUser = userService.getLoginUser(request);
        ChatWindows chatWindows = new ChatWindows();
        BeanUtils.copyProperties(chatWindowsEditRequest, chatWindows);
        // 参数校验
        chatWindowsService.validChatWindows(chatWindows, false);
        long id = chatWindowsEditRequest.getId();
        // 判断是否存在
        ChatWindows oldChatWindows = chatWindowsService.getById(id);
        ThrowUtils.throwIf(oldChatWindows == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChatWindows.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chatWindowsService.updateById(chatWindows);
        return ResultUtils.success(result);
    }

}
