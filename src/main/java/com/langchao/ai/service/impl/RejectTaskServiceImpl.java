package com.langchao.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langchao.ai.model.entity.RejectTask;
import com.langchao.ai.service.RejectTaskService;
import com.langchao.ai.mapper.RejectTaskMapper;
import org.springframework.stereotype.Service;

/**
* @author 20406
* @description 针对表【reject_task(消息表)】的数据库操作Service实现
* @createDate 2024-07-18 17:14:16
*/
@Service
public class RejectTaskServiceImpl extends ServiceImpl<RejectTaskMapper, RejectTask>
    implements RejectTaskService{

}




