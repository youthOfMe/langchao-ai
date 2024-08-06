package com.langchao.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langchao.ai.annotation.AuthCheck;
import com.langchao.ai.common.BaseResponse;
import com.langchao.ai.common.PageRequest;
import com.langchao.ai.common.ResultUtils;
import com.langchao.ai.constant.UserConstant;
import com.langchao.ai.model.entity.Model;
import com.langchao.ai.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 模型接口
 */
@RestController
@RequestMapping("/model")
@Slf4j
public class ModelController {

    @Resource
    private ModelService modelService;

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
}
