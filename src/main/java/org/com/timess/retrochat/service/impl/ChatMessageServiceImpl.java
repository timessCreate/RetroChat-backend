package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.ChatMessageMapper;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.service.ChatMessageService;
import org.springframework.stereotype.Service;

/**
 * 聊天消息表 服务层实现。
 *
 * @author eternal
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>  implements ChatMessageService{

}
