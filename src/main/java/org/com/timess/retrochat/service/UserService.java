package org.com.timess.retrochat.service;

import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.model.dto.user.UserLoginRequest;
import org.com.timess.retrochat.model.entity.User;

/**
 *  服务层。
 *
 * @author eternal
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param username
     * @param password
     * @param email
     * @param verifyCode
     */
    void userRegister(String username, String password, String email, String verifyCode);

    /**
     * @param
     * @return
     */
    String userLogin(UserLoginRequest userLoginRequest);

    /**
     * 退出登录
     * @param request
     * @return
     */
    Boolean logout(HttpServletRequest request);

    /**
     * 验证用户是否存在
     * @param id
     * @param username
     * @return
     */
    boolean existsUser(long id, String username);
}
