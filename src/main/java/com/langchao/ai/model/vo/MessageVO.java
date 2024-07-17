package com.langchao.ai.model.vo;

import com.langchao.ai.model.entity.Message;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子视图
 *

 */
@Data
public class MessageVO implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 窗口ID
     */
    private Long chatWindowId;

    /**
     * 消息类型 0 = 用户信息 1 = AI推送信息 2 = 警告信息 3 = 提示信息 4 = 推荐信息
     */
    private Integer type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 包装类转对象
     *
     * @param messageVO
     * @return
     */
    public static Message voToObj(MessageVO messageVO) {
        if (messageVO == null) {
            return null;
        }
        Message message = new Message();
        BeanUtils.copyProperties(messageVO, message);
        return message;
    }

    /**
     * 对象转包装类
     *
     * @param messageVO
     * @return
     */
    public static MessageVO objToVo(Message message) {
        if (message == null) {
            return null;
        }
        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(message, messageVO);
        return messageVO;
    }
}
