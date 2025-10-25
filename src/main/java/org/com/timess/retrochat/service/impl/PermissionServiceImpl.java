package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.PermissionMapper;
import org.com.timess.retrochat.model.entity.user.Permission;
import org.com.timess.retrochat.service.PermissionService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author eternal
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>  implements PermissionService {

}
