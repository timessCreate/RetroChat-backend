package org.com.timess.retrochat.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author eternal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
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
     * 消息时间戳（精确到毫秒）
     */
    private long timestamp;

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
}