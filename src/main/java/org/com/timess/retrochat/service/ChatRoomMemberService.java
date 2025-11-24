package org.com.timess.retrochat.service;

import com.mybatisflex.core.service.IService;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.com.timess.retrochat.model.entity.chat.ChatRoomMember;
import org.springframework.transaction.annotation.Transactional;

/**
 * 聊天室成员表 服务层。
 *
 * @author eternal
 */
public interface ChatRoomMemberService extends IService<ChatRoomMember> {


     /**
      * 保存私聊房间成员信息的方法
      *
      * @param chatMessage 聊天消息对象，包含相关的聊天信息
      * @param chatRoom 聊天房间对象，包含房间相关信息
      * @return 返回一个布尔值，表示保存操作是否成功
      */
     @Transactional
     boolean savePrivateRoomMember(ChatMessage chatMessage, ChatRoom chatRoom);

}
