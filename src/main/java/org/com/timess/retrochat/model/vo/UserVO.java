package org.com.timess.retrochat.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回当前登录用户信息
 * @author eternal
 */
@Data
public class UserVO implements Serializable {
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
}
