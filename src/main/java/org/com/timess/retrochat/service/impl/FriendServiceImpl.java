package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.constant.ReviewStatusEnum;
import org.com.timess.retrochat.mapper.FriendMapper;
import org.com.timess.retrochat.model.entity.user.Friend;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友关系表 服务层实现。
 * @author eternal
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend>  implements FriendService {

    @Autowired
    FriendMapper friendMapper;

    /**
     * 获取好友列表
     * @param request
     * @return
     */
    @Override
    public List<UserMessageVO> getFriendList(HttpServletRequest request) {
        long userId = (long)request.getAttribute("userId");
        return friendMapper.getFriendList(userId);
    }

    /**
     * 添加好友
     * @param fromUserId
     * @param toUserId
     */
    @Override
    public void addFriend(long fromUserId, long toUserId) {
        Friend friend = new Friend();
        friend.setUserId(new BigInteger(String.valueOf(fromUserId)));
        friend.setFriendId(new BigInteger(String.valueOf(toUserId)));
        friend.setStatus(ReviewStatusEnum.PASS.getValue());
        friend.setTopOrder(0);
        friend.setCreatedAt(LocalDateTime.now());
        this.save(friend);
        Friend friend2 = new Friend();
        friend2.setUserId(new BigInteger(String.valueOf(toUserId)));
        friend2.setFriendId(new BigInteger(String.valueOf(fromUserId)));
        friend2.setStatus(ReviewStatusEnum.PASS.getValue());
        friend2.setTopOrder(0);
        friend2.setCreatedAt(LocalDateTime.now());
        this.save(friend2);
    }
}
