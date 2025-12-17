package org.com.timess.retrochat.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.model.vo.FriendRequestVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.com.timess.retrochat.model.entity.user.FriendRequests;
import org.com.timess.retrochat.service.FriendRequestsService;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 好友申请表 控制层。
 *
 * @author eternal
 */
@RestController
@RequestMapping("/friendRequests")
public class FriendRequestsController {

    @Autowired
    private FriendRequestsService friendRequestsService;

    /**
     * 保存好友申请表。
     */
    @PostMapping("save")
    public boolean save(@RequestBody FriendRequestVO friendRequestVO, HttpServletRequest request) {
        try{
            friendRequestsService.sendFriendRequest(friendRequestVO, request);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询所有好友申请表。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<FriendRequests> list(HttpServletRequest request) {
        try{
            return friendRequestsService.getFriendRequestsList(request);
        }catch (Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 处理好友申请
     */
    @PostMapping("handle")
    public boolean handle(@RequestBody FriendRequests friendRequests, HttpServletRequest request) {
        try{
            friendRequestsService.handleFriendRequest(friendRequests, request);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
