package org.com.timess.retrochat.model.dto.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.com.timess.retrochat.common.PageRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatPageRequest extends PageRequest {
    /**
     * 聊天室id
     */
    private String roomId;
}
