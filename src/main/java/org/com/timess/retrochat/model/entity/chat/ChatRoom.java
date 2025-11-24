package org.com.timess.retrochat.model.entity.chat;

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
 * 聊天室表 实体类。
 *
 * @author eternal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_room")
public class ChatRoom implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 聊天室ID
     */
    @Id(keyType = KeyType.Generator, value = "snowFlakeId")
    private Long id;

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
     * 创建者用户ID
     */
    private Long ownerId;

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
     * 是否活跃：0-已解散 1-活跃
     */
    private Integer isActive;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后一条消息内容（冗余）
     */
    private String lastMessageContent;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivityTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "is_delete", isLogicDelete = true)
    private Integer isDelete;
}
