package org.com.timess.retrochat.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.com.timess.retrochat.model.entity.user.Friend;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.provider.friend.FriendProvider;

import java.util.List;

/**
 * 好友关系表 映射层。
 *
 * @author eternal
 */
public interface FriendMapper extends BaseMapper<Friend> {

    /**
     * 获取关联用户信息
     * @param userId
     * @return
     */
    @SelectProvider(value = FriendProvider.class, method = "getFriendList")
    List<UserMessageVO> getFriendList(long userId);


    /**
     * 获取好友申请列表
     * @param userId
     * @return
     */
    @SelectProvider(value = FriendProvider.class, method = "getFriendRequestList")
    List<UserMessageVO> getFriendRequestList(long userId);

}
