package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.UserMapper;
import org.com.timess.retrochat.model.entity.User;
import org.com.timess.retrochat.service.UserService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author eternal
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

}
