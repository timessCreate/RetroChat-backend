package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.RoleMapper;
import org.com.timess.retrochat.model.entity.user.Role;
import org.com.timess.retrochat.service.RoleService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author eternal
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>  implements RoleService {

}
