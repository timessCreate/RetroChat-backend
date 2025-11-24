package org.com.timess.retrochat.service;

import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.model.dto.chat.ChatMessageDTO;
import org.com.timess.retrochat.model.dto.chat.ChatRoomDTO;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天室表 服务层。
 * @author eternal
 */
public interface ChatRoomService extends IService<ChatRoom> {

    /**
     * 新建聊天室
     * @param request
     * @return
     */
    boolean saveData(ChatRoomDTO chatRoomDTO, HttpServletRequest request);

    /**
     * 创建聊天室
     * @param
     * @return
     */
    @Transactional
    ChatRoom createPrivateChatRoom(ChatMessage chatMessage);

    /**
     * 更新聊天室信息
     * @return
     */
    boolean updateData(ChatRoomDTO chatRoomDTO);

    /**
     * 解散聊天室
     * @param id
     * @param request
     * @return
     */
    boolean delete(String id, HttpServletRequest request);
}
