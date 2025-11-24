package org.com.timess.retrochat.controller.chat;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.com.timess.retrochat.model.dto.chat.ChatRoomDTO;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.com.timess.retrochat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室表 控制层。
 *
 * @author eternal
 */
@Slf4j
@RestController
@RequestMapping("/chatRoom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    /**
     * 根据主键更新聊天室表。
     * @param chatRoomDTO 聊天室表
     */
    @PutMapping("update")
    public boolean update(@RequestBody ChatRoomDTO chatRoomDTO ) {
        return chatRoomService.updateData(chatRoomDTO);
    }

    /**
     * 查询所有聊天室表。
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatRoom> list() {
        return chatRoomService.list();
    }


    /**
     * 删除聊天室
     * @return 所有数据
     */
    @GetMapping("delete")
    public boolean list(String id, HttpServletRequest request) {
        return chatRoomService.delete(id, request);
    }

}
