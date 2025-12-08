package org.com.timess.retrochat.model.dto.chat;

import lombok.Data;
import org.com.timess.retrochat.common.PageRequest;

@Data
public class ChatPageRequest extends PageRequest {
    /**
     * 聊天室id
     */
    private String roomId;
}
