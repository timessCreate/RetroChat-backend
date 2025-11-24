package org.com.timess.retrochat.model.vo;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 返回当前登录用户信息和最近一条消息
 * @author eternal
 */
@Data
public class UserMessageVO implements Serializable {
    private static final long serialVersionUID = -6878355451312782724L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 好友备注
     */
    private String remark;

    /**
     * 指定置顶排序
     */
    private Integer topOrder;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户电话
     */
    private String phone;

    /**
     * 聊天室id
     */
    private Long roomId;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLogin;
    /**
     *  最新消息发送时间
     */
    private String lastActivityTime;

    /**
     * 最近一条消息
     */
    private String lastMessageContent;
}
