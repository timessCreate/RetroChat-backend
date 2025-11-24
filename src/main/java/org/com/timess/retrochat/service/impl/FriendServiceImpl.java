package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.mapper.FriendMapper;
import org.com.timess.retrochat.model.entity.user.Friend;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
