package org.com.timess.retrochat.controller.chat;

import com.mybatisflex.core.paginate.Page;
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
@RestController
@RequestMapping("/chatRoom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    /**
     * 保存聊天室表。
     *
     * @param chatRoom 聊天室表
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ChatRoom chatRoom) {
        return chatRoomService.save(chatRoom);
    }

    /**
     * 根据主键删除聊天室表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return chatRoomService.removeById(id);
    }

    /**
     * 根据主键更新聊天室表。
     *
     * @param chatRoom 聊天室表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody ChatRoom chatRoom) {
        return chatRoomService.updateById(chatRoom);
    }

    /**
     * 查询所有聊天室表。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatRoom> list() {
        return chatRoomService.list();
    }

    /**
     * 根据主键获取聊天室表。
     *
     * @param id 聊天室表主键
     * @return 聊天室表详情
     */
    @GetMapping("getInfo/{id}")
    public ChatRoom getInfo(@PathVariable Long id) {
        return chatRoomService.getById(id);
    }

    /**
     * 分页查询聊天室表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ChatRoom> page(Page<ChatRoom> page) {
        return chatRoomService.page(page);
    }

}
