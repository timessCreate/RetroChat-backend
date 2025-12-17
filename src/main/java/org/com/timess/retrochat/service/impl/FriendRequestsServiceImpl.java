package org.com.timess.retrochat.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.constant.ReviewStatusEnum;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.exception.ThrowUtils;
import org.com.timess.retrochat.mapper.FriendRequestsMapper;
import org.com.timess.retrochat.model.entity.user.FriendRequests;
import org.com.timess.retrochat.model.entity.user.User;
import org.com.timess.retrochat.model.vo.FriendRequestVO;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.model.vo.UserVO;
import org.com.timess.retrochat.service.FriendRequestsService;
import org.com.timess.retrochat.service.FriendService;
import org.com.timess.retrochat.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友申请表 服务层实现。
 *
 * @author eternal
 */
@Service
public class FriendRequestsServiceImpl extends ServiceImpl<FriendRequestsMapper, FriendRequests>  implements FriendRequestsService{

    @Resource
    private UserService userService;

    @Resource
    private FriendService friendService;

    /**
     * 发送好友申请
     * @param request
     */
    @Override
    public void sendFriendRequest(FriendRequestVO friendRequestVO,  HttpServletRequest request) {
        Long friendId = friendRequestVO.getToUserId();
        //校验是否存在
        User user = userService.getById(friendId);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        //校验是否已经是好友
        List<UserMessageVO> friendList = friendService.getFriendList(request);
        ThrowUtils.throwIf(friendList.stream().anyMatch(item -> NumberUtil.equals(item.getId(),friendId)), ErrorCode.PARAMS_ERROR, "已经是好友");
        UserVO loginUser = userService.getLoginUser(request);
        FriendRequests friendRequests = new FriendRequests();
        friendRequests.setFromUserId(loginUser.getId());
        friendRequests.setToUserId(friendId);
        friendRequests.setRequestMessage(friendRequestVO.getRequestMessage());
        friendRequests.setIsRead(false);
        friendRequests.setStatus(0);
        friendRequests.setSendTime(LocalDateTime.now());
        this.save(friendRequests);
    }

    /**
     * 获取好友申请列表
     */
    @Override
    public List<FriendRequests> getFriendRequestsList(HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("to_user_id", loginUser.getId());
        queryWrapper.eq("status", 0);
        return this.list(queryWrapper);
    }

    /**
     * 处理好友申请
     * @param friendRequests
     * @param request
     */
    @Override
    public void handleFriendRequest(FriendRequests friendRequests, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        User toUser = userService.getById(friendRequests.getToUserId());
        if(toUser == null){
            throw new RuntimeException("用户不存在");
        }
        //查询申请单
        FriendRequests requestsOrder = this.getById(friendRequests.getId());
        if(requestsOrder == null){
            throw new RuntimeException("好友申请不存在");
        }
        //判断是否是接收者
        if(!NumberUtil.equals(requestsOrder.getToUserId(), loginUser.getId())){
            throw new RuntimeException("无权限处理好友申请");
        }
        if(!NumberUtil.equals(requestsOrder.getFromUserId(),friendRequests.getFromUserId())
                || !NumberUtil.equals(requestsOrder.getToUserId(),friendRequests.getToUserId())) {
            throw new RuntimeException("好友申请不存在");
        }
        //更新
        boolean b = this.updateById(friendRequests);
        if(!b){
            throw new RuntimeException("处理好友申请失败");
        }
        if(NumberUtil.compare(friendRequests.getStatus(), ReviewStatusEnum.PASS.getValue()) == 0){
            friendService.addFriend(friendRequests.getFromUserId(), friendRequests.getToUserId());
        }
    }

}
