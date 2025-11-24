package org.com.timess.retrochat.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.com.timess.retrochat.model.entity.user.User;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.provider.user.UserProvider;

import java.util.List;

/**
 *  映射层。
 *
 * @author eternal
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 获取关联用户信息
     * @param userId
     * @return
     */
    @SelectProvider(value = UserProvider.class, method = "getFriendList")
    List<UserMessageVO> getFriendList(long userId);
}
