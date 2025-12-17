package org.com.timess.retrochat.service;

import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.model.entity.user.Friend;
import org.com.timess.retrochat.model.vo.UserMessageVO;

import java.util.List;

/**
 * 好友关系表 服务层。
 *
 * @author eternal
 */
public interface FriendService extends IService<Friend> {

    /**
     * 获取好友列表
     * @param request
     * @return
     */
    List<UserMessageVO> getFriendList(HttpServletRequest request);

    /**
     * 添加好友
     * @param fromUserId
     * @param toUserId
     */
    void addFriend(long fromUserId, long toUserId);
}
