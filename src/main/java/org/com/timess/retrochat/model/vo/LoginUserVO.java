package org.com.timess.retrochat.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 33363
 * 返回当前登录用户信息
 */
@Data
public class LoginUserVO implements Serializable {
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
     * 0--正常， 1--封禁
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;
}
