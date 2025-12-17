package org.com.timess.retrochat.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FriendRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待添加用户id
     */
    private Long toUserId;
    /**
     * 请求消息
     */
    private String requestMessage;

    /**
     * 请求状态
     */
    private Integer status;

}


