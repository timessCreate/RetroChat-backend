package org.com.timess.retrochat.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.model.dto.chat.ChatPageRequest;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.model.dto.chat.ChatMessageDTO;

import java.util.List;

/**
 * 聊天消息表 服务层。
 *
 * @author eternal
 */
public interface ChatMessageService extends IService<ChatMessage> {


    /**
     * 保存私聊消息
     * @param chatMessageDTO
     * @return
     */
    ChatMessage savePrivateMessage(ChatMessageDTO chatMessageDTO);

    /**
     * 保存群聊消息
     * @param chatMessageDTO
     * @return
     */
    boolean savePublicMessage(ChatMessageDTO chatMessageDTO);

    /**
     * 删除消息
     * @param id
     * @return
     */
    boolean deleteMessage(Long id);

    /**
     * 获取历史聊天记录
     * @param roomId
     * @return
     */
    List<ChatMessageDTO> getHistoryChatMessage(long roomId);

    /**
     * 分页获取历史聊天记录
     * @param request
     * @return
     */
    Page<ChatMessageDTO> getHistoryPageChatMessage(ChatPageRequest request);
}

