package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.ChatRoomMemberMapper;
import org.com.timess.retrochat.model.entity.chat.ChatRoomMember;
import org.com.timess.retrochat.service.ChatRoomMemberService;
import org.springframework.stereotype.Service;

/**
 * 聊天室成员表 服务层实现。
 *
 * @author eternal
 */
@Service
public class ChatRoomMemberServiceImpl extends ServiceImpl<ChatRoomMemberMapper, ChatRoomMember>  implements ChatRoomMemberService{

}
