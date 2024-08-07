package com.langchao.ai.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langchao.ai.annotation.AuthCheck;
import com.langchao.ai.common.BaseResponse;
import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.common.PageRequest;
import com.langchao.ai.common.ResultUtils;
import com.langchao.ai.constant.UserConstant;
import com.langchao.ai.exception.BusinessException;
import com.langchao.ai.exception.ThrowUtils;
import com.langchao.ai.model.dto.model.ModelAddRequest;
import com.langchao.ai.model.dto.model.PreMessageDTO;
import com.langchao.ai.model.entity.Model;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.service.ModelService;
import com.langchao.ai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 模型接口
 */
@RestController
@RequestMapping("/model")
@Slf4j
public class ModelController {

    @Resource
    private ModelService modelService;

    @Resource
    private UserService userService;

    /**
     * 分页获取模型列表
     *
     * @param pageRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Model>> listUserByPage(@RequestBody PageRequest pageRequest,
                                                    HttpServletRequest request) {
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        Page<Model> modelPage = modelService.page(new Page<>(current, size));
        return ResultUtils.success(modelPage);
    }

    /**
     * 创建模型
     *
     * @param modelAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addMessage(@RequestBody ModelAddRequest modelAddRequest, HttpServletRequest request) {
        if (modelAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验用户是否登录
        User loginUser = userService.getLoginUser(request);

        // 转换消息
        List<PreMessageDTO> preMessage = modelAddRequest.getPreMessage();
        ThrowUtils.throwIf(preMessage.size() > 10, ErrorCode.PARAMS_ERROR, "最多预设十条预设消息");
        List<String> conversation = modelAddRequest.getConversation();
        ThrowUtils.throwIf(conversation.size() > 10, ErrorCode.PARAMS_ERROR, "最多预设十条对话示例");
        Model model = new Model();
        BeanUtils.copyProperties(modelAddRequest, model);
        model.setPreMessage(JSONUtil.toJsonStr(preMessage));
        model.setConversation(JSONUtil.toJsonStr(conversation));
        // 校验参数
        modelService.validModel(model, true, loginUser);
        model.setUserId(loginUser.getId());
        // 保存数据
        boolean result = modelService.save(model);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "数据库错误");
        Long newModelId = model.getId();
        return ResultUtils.success(newModelId);
    }
}
