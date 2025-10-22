package org.com.timess.retrochat.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送验证码接口
 * @author eternal
 */
@Data
public class UserSendRegisterMailRequest implements Serializable {
    /**
     * 用户邮箱
     */
    private String email;

    private static final long serialVersionUID = 1L;
}
