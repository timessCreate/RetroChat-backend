package org.com.timess.retrochat.model.dto.user;

import lombok.Data;
import org.com.timess.retrochat.common.PageRequest;

import java.io.Serializable;

/**
 *
 * 用户查询请求
 * @author eternal
 */
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}
