package com.langchao.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langchao.ai.common.ErrorCode;
import com.langchao.ai.exception.BusinessException;
import com.langchao.ai.exception.ThrowUtils;
import com.langchao.ai.mapper.ModelMapper;
import com.langchao.ai.model.entity.Model;
import com.langchao.ai.model.entity.User;
import com.langchao.ai.service.ModelService;
import com.langchao.ai.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 20406
* @description 针对表【model(窗口表)】的数据库操作Service实现
* @createDate 2024-08-04 16:15:25
*/
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model>
    implements ModelService{

    @Resource
    private UserService userService;

    /**
     * 校验新增模型参数
     *
     * @param model
     * @param isAdd
     */
    @Override
    public void validModel(Model model, boolean isAdd, User loginUser) {
        if (model == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验类型
        Integer type = model.getType();
        if (type == 0) {
            boolean isAdmin = userService.isAdmin(loginUser);
            ThrowUtils.throwIf(isAdmin, ErrorCode.NO_AUTH_ERROR, "对不起您没有管理员权限");
        }

        // 校验名称
        String name = model.getName();
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "名称不可为空");
        ThrowUtils.throwIf(StringUtils.length(name) > 32, ErrorCode.PARAMS_ERROR,"名称不可大于32");

        // 系统设定
        String systemPrompt = model.getSystemPrompt();
        ThrowUtils.throwIf(StringUtils.isBlank(systemPrompt), ErrorCode.PARAMS_ERROR, "系统设定不可为空");
        ThrowUtils.throwIf(StringUtils.length(systemPrompt) > 1024, ErrorCode.PARAMS_ERROR, "设定不可大于1024");

        // 校验封面头像
        String coverUrl = model.getCoverUrl();

        // 校验介绍
        String introduce = model.getIntroduce();
        ThrowUtils.throwIf(StringUtils.length(introduce) > 64, ErrorCode.PARAMS_ERROR, "介绍参数不可大于64");

        // 校验描述
        String description = model.getDescription();
        ThrowUtils.throwIf(StringUtils.length(description) > 1024, ErrorCode.PARAMS_ERROR, "描述参数不可大于1024");

        // 校验对话实例
        String conversation = model.getConversation();
        String preMessage = model.getPreMessage();

        String category = model.getCategory();

        // 校验可见性
        Integer visable = model.getVisable();
        ThrowUtils.throwIf(!(visable == 0 || visable == 1), ErrorCode.PARAMS_ERROR, "可见性必须为0/1");

        // 校验关联消息等
        Integer linkedMessage = model.getLinkedMessage();
        ThrowUtils.throwIf(linkedMessage > 40 || linkedMessage < 0, ErrorCode.PARAMS_ERROR, "关联消息必须在0-40区间");
        Integer random = model.getRandom();
        ThrowUtils.throwIf(random > 200 || random < 0, ErrorCode.PARAMS_ERROR, "随机值必须在 0-2区间");
        Integer fresh = model.getFresh();
        ThrowUtils.throwIf(fresh > 200 || fresh < -200, ErrorCode.PARAMS_ERROR, "新鲜度必须在 -2-2区间");
        Integer repeatNum = model.getRepeatNum();
        ThrowUtils.throwIf(repeatNum > 200 || repeatNum < -200, ErrorCode.PARAMS_ERROR, "重复率必须在 -2-2区间");
        // 创建时，参数不能为空
        if (isAdd) {

        }
    }
}




