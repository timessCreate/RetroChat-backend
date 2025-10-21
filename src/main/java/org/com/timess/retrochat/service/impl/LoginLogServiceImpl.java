package org.com.timess.retrochat.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.com.timess.retrochat.mapper.LoginLogMapper;
import org.com.timess.retrochat.model.entity.LoginLog;
import org.com.timess.retrochat.service.LoginLogService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author eternal
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog>  implements LoginLogService {

}
