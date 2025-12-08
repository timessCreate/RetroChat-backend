//package org.com.timess.retrochat.controller.chat;
//
//import com.mybatisflex.core.paginate.Page;
//import com.qcloud.cos.COSClient;
//import com.qcloud.cos.exception.CosClientException;
//import com.qcloud.cos.exception.CosServiceException;
//import com.qcloud.cos.model.Bucket;
//import jakarta.annotation.Resource;
//import org.com.timess.retrochat.model.entity.chat.ChatRoomMember;
//import org.com.timess.retrochat.service.ChatRoomMemberService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 聊天室成员表 控制层。
// *
// * @author eternal
// */
//@RestController
//@RequestMapping("/chatRoomMember")
//public class ChatRoomMemberController {
//
//    @Autowired
//    private ChatRoomMemberService chatRoomMemberService;
//
//    /**
//     * 保存聊天室成员表。
//     *
//     * @param chatRoomMember 聊天室成员表
//     * @return {@code true} 保存成功，{@code false} 保存失败
//     */
//    @PostMapping("save")
//    public boolean save(@RequestBody ChatRoomMember chatRoomMember) {
//        return chatRoomMemberService.save(chatRoomMember);
//    }
//
//    /**
//     * 根据主键删除聊天室成员表。
//     *
//     * @param id 主键
//     * @return {@code true} 删除成功，{@code false} 删除失败
//     */
//    @DeleteMapping("remove/{id}")
//    public boolean remove(@PathVariable Long id) {
//        return chatRoomMemberService.removeById(id);
//    }
//
//    /**
//     * 根据主键更新聊天室成员表。
//     *
//     * @param chatRoomMember 聊天室成员表
//     * @return {@code true} 更新成功，{@code false} 更新失败
//     */
//    @PutMapping("update")
//    public boolean update(@RequestBody ChatRoomMember chatRoomMember) {
//        return chatRoomMemberService.updateById(chatRoomMember);
//    }
//
//    /**
//     * 查询所有聊天室成员表。
//     *
//     * @return 所有数据
//     */
//    @GetMapping("list")
//    public List<ChatRoomMember> list() {
//        return chatRoomMemberService.list();
//    }
//
//    /**
//     * 根据主键获取聊天室成员表。
//     *
//     * @param id 聊天室成员表主键
//     * @return 聊天室成员表详情
//     */
//    @GetMapping("getInfo/{id}")
//    public ChatRoomMember getInfo(@PathVariable Long id) {
//        return chatRoomMemberService.getById(id);
//    }
//
//    /**
//     * 分页查询聊天室成员表。
//     *
//     * @param page 分页对象
//     * @return 分页对象
//     */
//    @GetMapping("page")
//    public Page<ChatRoomMember> page(Page<ChatRoomMember> page) {
//        return chatRoomMemberService.page(page);
//    }
//
//}
