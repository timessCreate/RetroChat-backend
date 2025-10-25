package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.ChatRoomMapper;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.com.timess.retrochat.service.ChatRoomService;
import org.springframework.stereotype.Service;

/**
 * 聊天室表 服务层实现。
 *
 * @author eternal
 */
@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom>  implements ChatRoomService{

}
