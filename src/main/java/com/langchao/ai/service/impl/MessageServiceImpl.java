package com.langchao.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.constant.CommonConstant;
import com.langchao.ai.exception.BusinessException;
import com.langchao.ai.exception.ThrowUtils;
import com.langchao.ai.manager.AiManager;
import com.langchao.ai.manager.RedisLimitManager;
import com.langchao.ai.mapper.MessageMapper;
import com.langchao.ai.model.dto.message.MessageQueryRequest;
import com.langchao.ai.model.dto.message.MessageSendRequest;
import com.langchao.ai.model.entity.ChatWindows;
import com.langchao.ai.model.entity.Message;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.model.enums.MessageEnum;
import com.langchao.ai.model.vo.MessageSendVO;
import com.langchao.ai.model.vo.MessageVO;
import com.langchao.ai.service.ChatWindowsService;
import com.langchao.ai.service.MessageService;
import com.langchao.ai.service.UserService;
import com.langchao.ai.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
* @author 20406
* @description 针对表【message(消息表)】的数据库操作Service实现
* @createDate 2024-07-17 14:15:22
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{
    @Resource
    private UserService userService;

    @Resource
    private ChatWindowsService chatWindowsService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimitManager redisLimitManager;

    @Override
    public void validMessage(Message message, boolean add) {
        if (message == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = message.getUserId();
        Long chatWindowId = message.getChatWindowId();
        Integer type = message.getType();
        String content = message.getContent();
        // 创建时，参数不能为空
        // 查询用户是否存在
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在！");
        // 查询窗口是否存在
        ChatWindows chatWindows = chatWindowsService.getById(chatWindowId);
        ThrowUtils.throwIf(chatWindows == null, ErrorCode.PARAMS_ERROR, "聊天窗口不存在！");
        // 判断类型信息是否正确
        MessageEnum typeEnum = MessageEnum.getEnumByValue(type);
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "窗口类型错误！");
        // 判断会话内容 用户可能会发空格
        ThrowUtils.throwIf(StringUtils.isEmpty(content), ErrorCode.PARAMS_ERROR, "内容不可为空！");
    }

    /**
     * 获取查询包装类
     *
     * @param messageQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Message> getQueryWrapper(MessageQueryRequest messageQueryRequest) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        if (messageQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = messageQueryRequest.getSearchText();
        String sortField = messageQueryRequest.getSortField();
        String sortOrder = messageQueryRequest.getSortOrder();
        Long id = messageQueryRequest.getId();
        Long userId = messageQueryRequest.getUserId();
        Long notId = messageQueryRequest.getNotId();
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
    public MessageVO getMessageVO(Message message, HttpServletRequest request) {
        return MessageVO.objToVo(message);
    }

    @Override
    public Page<MessageVO> getMessageVOPage(Page<Message> messagePage, HttpServletRequest request) {
        List<Message> messageList = messagePage.getRecords();
        Page<MessageVO> messageVOPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        if (CollUtil.isEmpty(messageList)) {
            return messageVOPage;
        }
        return messageVOPage;
    }

    /**
     * 发送消息
     * @param messageSendRequest
     * @param request
     * @return
     */
    @Override
    public MessageSendVO sendMessage(MessageSendRequest messageSendRequest, HttpServletRequest request) {
        // 校验是否登录
        User loginUser = userService.getLoginUser(request);
        // 校验消息
        String content = messageSendRequest.getContent();
        ThrowUtils.throwIf(StringUtils.isEmpty(content), ErrorCode.PARAMS_ERROR, "消息不可为空！");
        ThrowUtils.throwIf(content.length() > 512, ErrorCode.PARAMS_ERROR, "消息不可长于512");
        // 校验当前窗口是否为用户可用
        long chatWindowId = messageSendRequest.getChatWindowId();
        ThrowUtils.throwIf(chatWindowId <= 0, ErrorCode.PARAMS_ERROR, "当前窗口ID不可为空");
        ChatWindows chatWindows = chatWindowsService.getById(chatWindowId);
        ThrowUtils.throwIf(chatWindows == null, ErrorCode.PARAMS_ERROR, "请求窗口不存在");
        ThrowUtils.throwIf(!Objects.equals(chatWindows.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "非法请求");
        // 校验消息类型是否合法
        Integer type = messageSendRequest.getType();
        MessageEnum enumByValue = MessageEnum.getEnumByValue(type);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "消息类型不合法");

        // 启动限流器
        redisLimitManager.doRateLimit("sendMessageByAi_" + loginUser.getId());

        // 存入消息
        Message message = new Message();
        message.setUserId(loginUser.getId());
        message.setChatWindowId(chatWindowId);
        message.setType(type);
        message.setContent(content);
        boolean saveUserMes = this.save(message);
        ThrowUtils.throwIf(!saveUserMes, ErrorCode.SYSTEM_ERROR, "发送消息失败！");

        // todo 发送消息给AI
        long aiModeId = 1813472675464413185L;
        // 调用 AI
        String result = aiManager.doChat(aiModeId, content);
        // String result = "AI响应成功！";
        // 存储AI信息调用信息
        Message aiMessage = new Message();
        aiMessage.setUserId(loginUser.getId());
        aiMessage.setChatWindowId(chatWindowId);
        aiMessage.setType(type);
        aiMessage.setContent(result);
        boolean saveAiMes = this.save(aiMessage);
        ThrowUtils.throwIf(!saveAiMes, ErrorCode.SYSTEM_ERROR, "AI调用失败！");

        // 构造返回信息
        MessageSendVO messageSendVO = new MessageSendVO();
        messageSendVO.setUserMessage(content);
        messageSendVO.setAiMessage(result);

        return messageSendVO;
    }
}




