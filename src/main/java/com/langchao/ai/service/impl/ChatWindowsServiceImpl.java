package com.langchao.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.constant.CommonConstant;
import com.langchao.ai.exception.BusinessException;
import com.langchao.ai.exception.ThrowUtils;
import com.langchao.ai.manager.RedisLimitManager;
import com.langchao.ai.mapper.ChatWindowsMapper;
import com.langchao.ai.model.dto.chatwindows.ChatWindowsQueryRequest;
import com.langchao.ai.model.entity.ChatWindows;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.model.enums.ChatWindowsEnum;
import com.langchao.ai.model.vo.ChatWindowsVO;
import com.langchao.ai.service.ChatWindowsService;
import com.langchao.ai.service.UserService;
import com.langchao.ai.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 20406
* @description 针对表【chat_windows(窗口表)】的数据库操作Service实现
* @createDate 2024-07-17 12:40:43
*/
@Service
public class ChatWindowsServiceImpl extends ServiceImpl<ChatWindowsMapper, ChatWindows>
    implements ChatWindowsService{

    @Resource
    private UserService userService;

    @Resource
    private RedisLimitManager redisLimitManager;

    // 创建窗口时的锁
    private static final String CHAT_WINDOW_USER_ID_KEY = "chat_window_user_id_key";

    @Override
    public void validChatWindows(ChatWindows chatWindows, boolean add) {
        if (chatWindows == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = chatWindows.getUserId();
        Integer type = chatWindows.getType();
        String title = chatWindows.getTitle();
        // 创建时，参数不能为空
        // 查询用户是否存在
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在！");
        // 判断类型信息是否正确
        ChatWindowsEnum typeEnum = ChatWindowsEnum.getEnumByValue(type);
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "窗口类型错误！");
        // 判断会话标题 用户可能会发空格
        ThrowUtils.throwIf(StringUtils.isEmpty(title), ErrorCode.PARAMS_ERROR, "标题不可为空！");
    }

    /**
     * 获取查询包装类
     *
     * @param chatWindowsQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ChatWindows> getQueryWrapper(ChatWindowsQueryRequest chatWindowsQueryRequest) {
        QueryWrapper<ChatWindows> queryWrapper = new QueryWrapper<>();
        if (chatWindowsQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = chatWindowsQueryRequest.getSearchText();
        String sortField = chatWindowsQueryRequest.getSortField();
        String sortOrder = chatWindowsQueryRequest.getSortOrder();
        Long id = chatWindowsQueryRequest.getId();
        Long userId = chatWindowsQueryRequest.getUserId();
        Long notId = chatWindowsQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public ChatWindowsVO getChatWindowsVO(ChatWindows chatWindows, HttpServletRequest request) {
        return ChatWindowsVO.objToVo(chatWindows);
    }

    @Override
    public Page<ChatWindowsVO> getChatWindowsVOPage(Page<ChatWindows> chatWindowsPage, HttpServletRequest request) {
        List<ChatWindows> chatWindowsList = chatWindowsPage.getRecords();
        Page<ChatWindowsVO> chatWindowsVOPage = new Page<>(chatWindowsPage.getCurrent(), chatWindowsPage.getSize(), chatWindowsPage.getTotal());
        if (CollUtil.isEmpty(chatWindowsList)) {
            return chatWindowsVOPage;
        }
        return chatWindowsVOPage;
    }

    @Override
    public Long createChatWindows(Integer type, User loginUser, String title) {

        Long userId = loginUser.getId();

        // 限流器 限制一个用户三秒内只能访问一次
        redisLimitManager.doRateLimit(CHAT_WINDOW_USER_ID_KEY + userId, 1, 3);

        // 限制用户可以创建窗口的数量 最多创建十个
        QueryWrapper<ChatWindows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long chatWindowCount = this.count(queryWrapper);
        ThrowUtils.throwIf(chatWindowCount > 10, ErrorCode.OPERATION_ERROR, "您最多创建十个窗口！");

        // 创建窗口
        ChatWindows chatWindows = new ChatWindows();
        chatWindows.setUserId(userId);
        chatWindows.setTitle(title);
        chatWindows.setType(type);
        boolean isSuccess = this.save(chatWindows);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.SYSTEM_ERROR, "创建会话失败！");

        return chatWindows.getId();
    }
}




