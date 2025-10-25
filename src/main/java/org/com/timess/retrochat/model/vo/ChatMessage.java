package org.com.timess.retrochat.model.vo;

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
public class ChatMessage {

    /**
     * 内容
     */
    private String content;

    /**
     * 发送者
     */
    private String sender;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}