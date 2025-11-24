package org.com.timess.retrochat.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.com.timess.retrochat.enums.RoleEnums;
import org.com.timess.retrochat.mapper.ChatRoomMemberMapper;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.com.timess.retrochat.model.entity.chat.ChatRoomMember;
import org.com.timess.retrochat.model.entity.user.User;
import org.com.timess.retrochat.service.ChatRoomMemberService;
import org.com.timess.retrochat.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 聊天室成员表 服务层实现。
 *
 * @author eternal
 */
@Slf4j
@Service
public class ChatRoomMemberServiceImpl extends ServiceImpl<ChatRoomMemberMapper, ChatRoomMember>  implements ChatRoomMemberService{

    @Resource
    @Lazy
    private ChatRoomMemberService self;

    @Resource
    private UserService userService;

    @Override
    public boolean savePrivateRoomMember(ChatMessage chatMessage, ChatRoom chatRoom) {
        try{
            //插入群主信息
            ChatRoomMember chatRoomMember = new ChatRoomMember();
            chatRoomMember.setRoomId(chatRoom.getId());
            chatRoomMember.setUserId(chatMessage.getSenderId());
            chatRoomMember.setUserNickname(chatMessage.getSenderName());
            chatRoomMember.setRole(RoleEnums.OWNER.getType());
            chatRoomMember.setJoinTime(LocalDateTime.now());
            chatRoomMember.setIsMuted(0);
            self.save(chatRoomMember);
            //插入私聊对象信息
            ChatRoomMember chatRoomMember2 = new ChatRoomMember();
            chatRoomMember2.setRoomId(chatRoom.getId());
            chatRoomMember2.setUserId(chatMessage.getReceiverId());
            //根据receiverId获取到对应的用户名称
            User receiverUser = userService.getById(chatMessage.getReceiverId());
            chatRoomMember2.setUserNickname(ObjectUtil.isEmpty(receiverUser) ? "匿名用户" : receiverUser.getUsername());
            chatRoomMember2.setRole(RoleEnums.OWNER.getType());
            chatRoomMember2.setJoinTime(LocalDateTime.now());
            chatRoomMember2.setIsMuted(0);
            self.save(chatRoomMember2);
        }catch (Exception e){
            log.error("保存用户成员信息失败" + e.getMessage());
        }
        return true;
    }
}
