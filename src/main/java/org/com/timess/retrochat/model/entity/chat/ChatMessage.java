package org.com.timess.retrochat.model.entity.chat;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.timess.retrochat.model.dto.chat.ChatMessageDTO;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息表 实体类。
 *
 * @author eternal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_message")
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Id(keyType = KeyType.Generator, value = "snowFlakeId")
    private Long id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送者用户ID
     */
    private Long senderId;

    /**
     * 发送者用户名（冗余存储）
     */
    private String senderName;

    /**
     * 消息类型：1-群聊 2-私聊 3-系统消息
     */
    private Integer messageType;

    /**
     * 聊天室ID（群聊时使用）
     */
    private Long chatRoomId;

    /**
     * 接收者用户ID（私聊时使用）
     */
    private Long receiverId;

    /**
     * 接收者用户名（冗余存储）
     */
    private String receiverName;

    /**
     * 消息时间戳（精确到毫秒）
     */
    private LocalDateTime timestamp;

    /**
     * 消息格式：1-文本 2-图片 3-文件 4-语音
     */
    private Integer messageFormat;

    /**
     * 文件/图片/语音URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 回复的消息ID
     */
    private Long replyToId;

    /**
     * 是否已读：0-未读 1-已读（私聊使用）
     */
    private Integer isRead;

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


    public ChatMessageDTO getDTO(){
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        BeanUtils.copyProperties(this, chatMessageDTO);
        return chatMessageDTO;
    }

    public ChatMessageDTO getDTO(ChatMessage chatMessage){
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        BeanUtils.copyProperties(chatMessage, chatMessageDTO);
        return chatMessageDTO;
    }


}
