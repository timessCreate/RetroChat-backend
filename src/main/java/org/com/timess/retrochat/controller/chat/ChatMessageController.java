package org.com.timess.retrochat.controller.chat;

import com.mybatisflex.core.paginate.Page;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息表 控制层。
 *
 * @author eternal
 */
@RestController
@RequestMapping("/chatMessage")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 保存聊天消息表。
     *
     * @param chatMessage 聊天消息表
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ChatMessage chatMessage) {
        return chatMessageService.save(chatMessage);
    }

    /**
     * 根据主键删除聊天消息表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return chatMessageService.removeById(id);
    }

    /**
     * 查询所有聊天消息表。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatMessage> list() {
        return chatMessageService.list();
    }

    /**
     * 分页查询聊天消息表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ChatMessage> page(Page<ChatMessage> page) {
        return chatMessageService.page(page);
    }

}
