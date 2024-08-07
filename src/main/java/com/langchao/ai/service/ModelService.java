package com.langchao.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.langchao.ai.model.entity.Model;
import com.langchao.ai.model.entity.User;

/**
* @author 20406
* @description 针对表【model(窗口表)】的数据库操作Service
* @createDate 2024-08-07 13:04:32
*/
public interface ModelService extends IService<Model> {

    /**
     * 校验新增模型参数
     *
     * @param model
     * @param isAdd
     */
    void validModel(Model model, boolean isAdd, User loginUser);
}
