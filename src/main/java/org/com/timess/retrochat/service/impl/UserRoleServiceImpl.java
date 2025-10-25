package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.UserRoleMapper;
import org.com.timess.retrochat.model.entity.user.UserRole;
import org.com.timess.retrochat.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author eternal
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>  implements UserRoleService {

}
