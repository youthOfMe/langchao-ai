package com.langchao.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.config.CallRunable;
import com.langchao.ai.constant.CommonConstant;
import com.langchao.ai.exception.BusinessException;
import com.langchao.ai.exception.ThrowUtils;
import com.langchao.ai.manager.AiManager;
import com.langchao.ai.manager.AiMessageV1;
import com.langchao.ai.manager.NewAiManager;
import com.langchao.ai.manager.RedisLimitManager;
import com.langchao.ai.mapper.MessageMapper;
import com.langchao.ai.mapper.RejectTaskMapper;
import com.langchao.ai.model.dto.message.MessageQueryRequest;
import com.langchao.ai.model.dto.message.MessageSendRequest;
import com.langchao.ai.model.entity.ChatWindows;
import com.langchao.ai.model.entity.Message;
import com.langchao.ai.model.entity.RejectTask;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.model.enums.MessageEnum;
import com.langchao.ai.model.vo.MessageSendVO;
import com.langchao.ai.model.vo.MessageVO;
import com.langchao.ai.service.ChatWindowsService;
import com.langchao.ai.service.MessageService;
import com.langchao.ai.service.RejectTaskService;
import com.langchao.ai.service.UserService;
import com.langchao.ai.utils.DoAsyncAI;
import com.langchao.ai.utils.SqlUtils;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RejectTaskService rejectTaskService;

    @Resource
    private RejectTaskMapper rejectTaskMapper;

    @Resource
    @Lazy
    private DoAsyncAI doAsyncAI;

    @Resource
    private NewAiManager newAiManager;

    @Resource
    private AiMessageV1 aiMessageV1;

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

        // 启动限流器
        redisLimitManager.doRateLimit("sendMessageByAi_" + loginUser.getId(),1, 5);

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

        // 查询会话状态
        Integer canSend = chatWindows.getCanSend();
        ThrowUtils.throwIf(canSend == 1, ErrorCode.OPERATION_ERROR, "AI消息正在生成中！");

        // 存入消息
        Message message = new Message();
        message.setUserId(loginUser.getId());
        message.setChatWindowId(chatWindowId);
        message.setType(type);
        message.setContent(content);
        boolean saveUserMes = this.save(message);
        ThrowUtils.throwIf(!saveUserMes, ErrorCode.SYSTEM_ERROR, "发送消息失败！");

        if (Objects.equals(chatWindows.getTitle(), "新建对话")) {
            chatWindows.setTitle(content);
            chatWindowsService.updateById(chatWindows);
        }

        // 改变会话状态 不允许用户在正在生成中的时候进行调用会话AI
        chatWindows.setCanSend(1);
        chatWindowsService.updateById(chatWindows);

        // 查询出来上一次AI发了什么
        QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
        messageQueryWrapper.eq("chatWindowId", chatWindowId);
        List<Message> messageList = this.list(messageQueryWrapper);
        // todo 发送消息给AI
        long aiModeId = 1813472675464413185L;
        // 调用 AI
        // String result = newAiManager.doSyncStableRequest("你是中国政务大师", content);

        // String result;
        // if (aiAssitantMessage == null) {
        //     result = newAiManager.doRequestAssistant("你是中国政务大师", content, null, String.valueOf(chatWindowId + loginUser.getId()));
        // } else {
        //     result = newAiManager.doRequestAssistant("你是中国政务大师", content, aiAssitantMessage.getContent(), String.valueOf(chatWindowId + loginUser.getId()));
        // }

        // String result = aiManager.doChat(aiModeId, content);
        // String result = "AI响应成功！";

        String result;
        List<ChatMessage> chatMessageList = new ArrayList<>();
        // 读取上下文消息
        for (Message sendMessage : messageList) {
            if (Objects.equals(sendMessage.getType(), MessageEnum.USER.getValue())) {
                chatMessageList.add(new ChatMessage(ChatMessageRole.USER.value(), sendMessage.getContent()));
            } else if (Objects.equals(sendMessage.getType(), MessageEnum.AI.getValue())) {
                chatMessageList.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), sendMessage.getContent()));
            }
        }
        result = aiMessageV1.doRequest(chatMessageList);

        // 防止用户聊着聊着就把AI窗口删了，此时AI窗口还没生成完
        ChatWindows oldChatWindows = chatWindowsService.getById(chatWindowId);
        if (oldChatWindows == null) {
            return null;
        }

        // 存储AI信息调用信息
        Message aiMessage = new Message();
        aiMessage.setUserId(loginUser.getId());
        aiMessage.setChatWindowId(chatWindowId);
        aiMessage.setType(MessageEnum.AI.getValue());
        aiMessage.setContent(result);
        boolean saveAiMes = this.save(aiMessage);
        ThrowUtils.throwIf(!saveAiMes, ErrorCode.SYSTEM_ERROR, "AI调用失败！");

        // 还原会话状态
        chatWindows.setCanSend(0);
        chatWindowsService.updateById(chatWindows);

        // 构造返回信息
        MessageSendVO messageSendVO = new MessageSendVO();
        messageSendVO.setUserMessage(content);
        messageSendVO.setAiMessage(result);

        return messageSendVO;
    }

    /**
     * 发送消息（异步化 + SSE推送）
     *
     * @param messageSendRequest
     * @param request
     * @return
     */
    @Override
    public SseEmitter sendMessageSse(MessageSendRequest messageSendRequest, HttpServletRequest request) {
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

        // 建立SSE连接
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 发送消息给AI
        CompletableFuture.runAsync(() -> {
            // todo 发送消息给AI
            long aiModeId = 1813472675464413185L;
            // 调用 AI
            String result = newAiManager.doSyncStableRequest("你是中国政务大师", content);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("message", result);
            // String result = "AI响应成功！";
            // 存储AI信息调用信息
            Message aiMessage = new Message();
            aiMessage.setUserId(loginUser.getId());
            aiMessage.setChatWindowId(chatWindowId);
            aiMessage.setType(MessageEnum.AI.getValue());
            aiMessage.setContent(result);
            boolean saveAiMes = this.save(aiMessage);
            ThrowUtils.throwIf(!saveAiMes, ErrorCode.SYSTEM_ERROR, "AI调用失败！");
            try {
                sseEmitter.send(JSONUtil.toJsonStr(hashMap));
                sseEmitter.complete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return sseEmitter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SseEmitter sendMessageAsync(MessageSendRequest messageSendRequest, HttpServletRequest request) throws IOException {
        // 校验是否登录
        User loginUser = userService.getLoginUser(request);
        // 校验消息
        String content = messageSendRequest.getContent();
        ThrowUtils.throwIf(StringUtils.isEmpty(content), ErrorCode.PARAMS_ERROR, "消息不可为空！");
        ThrowUtils.throwIf(content.length() > 512, ErrorCode.PARAMS_ERROR, "消息不可长于512");

        // 启动限流器
        redisLimitManager.doRateLimit("sendMessageByAi_" + loginUser.getId());

        // 校验当前窗口是否为用户可用
        long chatWindowId = messageSendRequest.getChatWindowId();
        ThrowUtils.throwIf(chatWindowId <= 0, ErrorCode.PARAMS_ERROR, "当前窗口ID不可为空");
        ChatWindows chatWindows = chatWindowsService.getById(chatWindowId);
        ThrowUtils.throwIf(chatWindows == null, ErrorCode.PARAMS_ERROR, "请求窗口不存在");
        // 判断窗口是否是用户创建的
        ThrowUtils.throwIf(!Objects.equals(chatWindows.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "非法请求");

        // 判断是否可以给AI继续发消息（AI是否正在生成中）
        ThrowUtils.throwIf(chatWindows.getCanSend() == 1, ErrorCode.OPERATION_ERROR, "AI正在生成中！");

        // 校验消息类型是否合法
        Integer type = messageSendRequest.getType();
        MessageEnum enumByValue = MessageEnum.getEnumByValue(type);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "消息类型不合法");

        // 改变会话状态 不允许用户在正在生成中的时候进行调用会话AI
        chatWindows.setCanSend(1);
        chatWindowsService.updateById(chatWindows);

        // 存入消息
        Message message = new Message();
        message.setUserId(loginUser.getId());
        message.setChatWindowId(chatWindowId);
        message.setType(type);
        message.setContent(content);
        boolean saveUserMes = this.save(message);
        ThrowUtils.throwIf(!saveUserMes, ErrorCode.SYSTEM_ERROR, "发送消息失败！");

        // 将任务推进到数据库中
        RejectTask rejectTask = new RejectTask();
        rejectTask.setUserId(loginUser.getId());
        rejectTask.setChatWindowId(chatWindowId);
        rejectTask.setTask(content);
        rejectTask.setIsNotify(1);
        boolean isSuccess = rejectTaskService.save(rejectTask);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.SYSTEM_ERROR, "系统出错！");

        // 判断任务数据库中是否还有更早的任务
        QueryWrapper<RejectTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isNotify", 0);
        RejectTask task = rejectTaskService.getOne(queryWrapper);
        if (task != null) {
            rejectTask.setIsNotify(0);
            isSuccess = rejectTaskService.updateById(rejectTask);
            ThrowUtils.throwIf(!isSuccess, ErrorCode.SYSTEM_ERROR, "系统出错！");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统负载严重");
        }

        // 建立SSE连接
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 异步化系统
        CallRunable callRunable = new CallRunable(() -> {
            doAsyncAI.asyncUserAI(loginUser.getId(), chatWindowId, sseEmitter, chatWindows, rejectTask, content);
        }, chatWindowId, loginUser.getId(), content, sseEmitter);

        threadPoolExecutor.execute(callRunable);

        return sseEmitter;
    }

    /**
     * 获取消息列表
     *
     * @param chatWindowsId
     * @param request
     * @return
     */
    @Override
    public List<Message> listMessage(Long chatWindowsId, HttpServletRequest request) {
        ThrowUtils.throwIf(chatWindowsId == null || chatWindowsId <= 0, ErrorCode.PARAMS_ERROR, "chatWindowsID不可为空");
        // 查一下窗口存在吗
        ChatWindows chatWindows = chatWindowsService.getById(chatWindowsId);
        ThrowUtils.throwIf(chatWindows == null, ErrorCode.PARAMS_ERROR, "chatWindows不存在！");

        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chatWindowId", chatWindowsId);
        List<Message> messageList = this.list(queryWrapper);
        return messageList;
    }

}




