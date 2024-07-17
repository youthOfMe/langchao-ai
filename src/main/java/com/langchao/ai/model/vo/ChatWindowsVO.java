package com.langchao.ai.model.vo;

import com.langchao.ai.model.entity.ChatWindows;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子视图
 *

 */
@Data
public class ChatWindowsVO implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 窗口类型 1 = 紧急政务窗口 0 = 普通窗口
     */
    private Integer type;

    /**
     * 会话标题
     */
    private String title;

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
     * @param chatWindowsVO
     * @return
     */
    public static ChatWindows voToObj(ChatWindowsVO chatWindowsVO) {
        if (chatWindowsVO == null) {
            return null;
        }
        ChatWindows chatWindows = new ChatWindows();
        BeanUtils.copyProperties(chatWindowsVO, chatWindows);
        return chatWindows;
    }

    /**
     * 对象转包装类
     *
     * @param chatWindows
     * @return
     */
    public static ChatWindowsVO objToVo(ChatWindows chatWindows) {
        if (chatWindows == null) {
            return null;
        }
        ChatWindowsVO chatWindowsVO = new ChatWindowsVO();
        BeanUtils.copyProperties(chatWindows, chatWindowsVO);
        return chatWindowsVO;
    }
}
