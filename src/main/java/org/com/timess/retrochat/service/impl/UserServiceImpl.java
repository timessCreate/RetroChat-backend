package org.com.timess.retrochat.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.com.timess.retrochat.exception.BusinessException;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.exception.ThrowUtils;
import org.com.timess.retrochat.mapper.UserMapper;
import org.com.timess.retrochat.model.dto.user.UserLoginRequest;
import org.com.timess.retrochat.model.entity.User;
import org.com.timess.retrochat.service.UserService;
import org.com.timess.retrochat.utils.CommonUtils;
import org.com.timess.retrochat.utils.EmailApi;
import org.com.timess.retrochat.utils.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author eternal
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册校验
     */
    @Override
    public void userRegister(String username, String password, String email, String verifyCode) {
        //1.校验
        if (StrUtil.hasBlank(username, password, email, verifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不全");
        }
        //校验验证码是否存在
        String code = stringRedisTemplate.opsForValue().get(EmailApi.buildVerifyCodeKey(email));
        if (StringUtils.isEmpty(code) || !code.equals(verifyCode)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码过期或错误");
        }
        //校验账号名是否已经被使用
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号名已被使用");
        //验证邮箱是否已经被注册
        queryWrapper = new QueryWrapper();
        queryWrapper.eq("email", email);
        count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "邮箱已被注册，请直接登录");
        //密码加密
        String encryptPassword = CommonUtils.getEncryptPassword(password);
        //数据插入
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        user.setEmail(email);
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败，数据库异常");
    }

    /**
     * 登录功能实现
     *
     * @param userLoginRequest
     * @return
     */
    @Override
    public String userLogin(UserLoginRequest userLoginRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", userLoginRequest.getUsername());
        User currentUser = this.getOne(queryWrapper);
        //判定该用户是否存在
        ThrowUtils.throwIf(currentUser == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        //判定密码是否输入正确
        ThrowUtils.throwIf(
                !currentUser.getPassword().equals(CommonUtils.getEncryptPassword(userLoginRequest.getPassword())),
                ErrorCode.PARAMS_ERROR, "密码错误");
        //登录成功，生成token
        return JwtUtil.generateToken(currentUser.getUsername(), currentUser.getId());
    }

    @Override
    public Boolean logout(HttpServletRequest request) {
        return true;
    }

    @Override
    public boolean existsUser(long id, String username) {
        User loginUser = this.getById(id);
        return StringUtils.equals(loginUser.getUsername(), username);
    }
}
