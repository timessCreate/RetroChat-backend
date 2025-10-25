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
 * 聊天室成员表 实体类。
 *
 * @author eternal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_room_member")
public class ChatRoomMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = "snowFlakeId")
    private Long id;

    /**
     * 聊天室ID
     */
    private Long roomId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户在群内的昵称
     */
    private String userNickname;

    /**
     * 成员角色：1-普通成员 2-管理员 3-群主
     */
    private Integer role;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 最后阅读的消息ID
     */
    private Long lastReadMessageId;

    /**
     * 是否禁言：0-否 1-是
     */
    private Integer isMuted;

    /**
     * 禁言截止时间
     */
    private LocalDateTime muteUntil;

    /**
     * 是否删除
     */
    @Column(value = "is_delete", isLogicDelete = true)
    private Integer isDelete;

}
