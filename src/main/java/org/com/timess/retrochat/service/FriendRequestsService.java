package org.com.timess.retrochat.service;

import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.model.entity.user.FriendRequests;
import org.com.timess.retrochat.model.vo.FriendRequestVO;

import java.util.List;


/**
 * 好友申请表 服务层。
 *
 * @author eternal
 */

public interface FriendRequestsService extends IService<FriendRequests> {

    /**
     * 发送好友申请
     */
    void sendFriendRequest(FriendRequestVO friendRequestVO, HttpServletRequest request);

    /**
     * 获取好友申请列表
     * @param request
     * @return
     */
    List<FriendRequests> getFriendRequestsList(HttpServletRequest request);

    /**
     * 处理好友申请
     * @param friendRequests
     * @param request
     */
    void handleFriendRequest(FriendRequests friendRequests, HttpServletRequest request);
}
