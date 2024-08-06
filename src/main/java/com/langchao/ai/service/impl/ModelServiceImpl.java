package com.langchao.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langchao.ai.model.entity.Model;
import com.langchao.ai.service.ModelService;
import com.langchao.ai.mapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
* @author 20406
* @description 针对表【model(窗口表)】的数据库操作Service实现
* @createDate 2024-08-04 16:15:25
*/
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model>
    implements ModelService{

}




