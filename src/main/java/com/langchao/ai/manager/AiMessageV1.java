package com.langchao.ai.manager;

import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 具有上下文的AI
 */
@Component
public class AiMessageV1 {

    @Resource
    private ClientV4 clientV4;

    // 稳定的随机数
    public static final float STABLE_TEMPERATURE = 0.05f;

    // 不稳定的随机数
    public static final float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 通用请求
     *
     * @param messageList
     * @return
     */
    public String doRequest(List<ChatMessage> messageList) {
        return doRequest(messageList, Boolean.FALSE, STABLE_TEMPERATURE);
    }

    /**
     * 通用请求
     *
     * @param messageList
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(List<ChatMessage> messageList, Boolean stream, Float temperature) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messageList)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
        }
    }
}
