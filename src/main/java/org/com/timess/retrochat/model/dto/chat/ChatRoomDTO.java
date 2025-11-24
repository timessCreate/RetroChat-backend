package org.com.timess.retrochat.model.dto.chat;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天室表 dto对象
 *
 * @author eternal
 */
@Data
public class ChatRoomDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 聊天室名称
     */
    private String name;

    /**
     * 聊天室描述
     */
    private String description;

    /**
     * 聊天室类型：1-公开群聊 2-私密群聊 3-一对一私聊
     */
    private Integer type;

    /**
     * 最大成员数
     */
    private Integer maxMembers;

    /**
     * 当前成员数
     */
    private Integer currentMembers;

    /**
     * 聊天室头像
     */
    private String avatarUrl;

    /**
     * 群主id
     */
    private String ownerId;

}
