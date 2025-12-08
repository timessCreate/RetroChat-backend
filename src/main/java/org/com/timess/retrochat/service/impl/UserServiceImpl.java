package org.com.timess.retrochat.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.mail.Multipart;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.com.timess.retrochat.exception.BusinessException;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.exception.ThrowUtils;
import org.com.timess.retrochat.manager.CosManager;
import org.com.timess.retrochat.manager.CosUploadResult;
import org.com.timess.retrochat.mapper.UserMapper;
import org.com.timess.retrochat.model.dto.user.UserLoginRequest;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.com.timess.retrochat.model.entity.user.User;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.model.vo.UserVO;
import org.com.timess.retrochat.service.TokenBlacklistService;
import org.com.timess.retrochat.service.UserService;
import org.com.timess.retrochat.utils.CommonUtils;
import org.com.timess.retrochat.utils.EmailApi;
import org.com.timess.retrochat.utils.ImageValidatorUtils;
import org.com.timess.retrochat.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 *
 * @author eternal
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    TokenBlacklistService tokenBlacklistService;

    @Resource
    CosManager cosManager;

    @Value("${cos.paths.default_avatar}")
    private String defaultAvatar;

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
        ThrowUtils.throwIf(count > 0, ErrorCode.USER_NAME_ERROR, "账号名已被使用");
        //验证邮箱是否已经被注册
        queryWrapper = new QueryWrapper();
        queryWrapper.eq("email", email);
        count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.EMAIL_ERROR, "邮箱已被注册，请直接登录");
        //密码加密
        String encryptPassword = CommonUtils.getEncryptPassword(password);
        //数据插入
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        user.setEmail(email);
        //设置默认头像
        user.setUserAvatar(defaultAvatar);
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
        ThrowUtils.throwIf(currentUser == null, ErrorCode.USER_NAME_ERROR, "用户不存在");
        //判定密码是否输入正确
        ThrowUtils.throwIf(
                !currentUser.getPassword().equals(CommonUtils.getEncryptPassword(userLoginRequest.getPassword())),
                ErrorCode.PASSWORD_ERROR, "密码错误");
        //登录成功，生成token
        return JwtUtil.generateToken(currentUser.getUsername(), currentUser.getId());
    }

    @Override
    public Boolean logout(HttpServletRequest request) {
        //将token加入黑名单
        String authorization = request.getHeader("Authorization");
        tokenBlacklistService.addToBlacklist(authorization);
        return true;
    }

    @Override
    public boolean existsUser(long id, String username) {
        User loginUser = this.getById(id);
        return StringUtils.equals(loginUser.getUsername(), username);
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        Long userId = Long.valueOf((String) request.getAttribute("userId"));
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录或已注销");
        UserVO loginUserVO = new UserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 更新用户信息
     * @param userVO
     * @param request
     * @return
     */
    @Override
    public UserVO updateProfile(UserVO userVO, HttpServletRequest request) {
        UserVO loginUser = this.getLoginUser(request);
        if(loginUser == null || !NumberUtil.equals(loginUser.getId(), userVO.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "待更新用户信息与当前登陆用户不是同一人");
        }
        User user = new User();
        BeanUtils.copyProperties(userVO, user);
        if(this.updateById(user)){
            return userVO;
        }else{
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户信息失败，请联系联系管理员");
        }
    }

    /**
     * 更新用户头像
     * @param file
     * @param request
     * @return
     */
    @Override
    public String updateAvatar(MultipartFile file, HttpServletRequest request) {
        UserVO loginUser = this.getLoginUser(request);
        ImageValidatorUtils.ValidationResult validationResult = ImageValidatorUtils.validateImage(file);
        if(!validationResult.isValid()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片类型不支持");
        }
        CosUploadResult cosUploadResult;
        try {
            cosUploadResult = cosManager.uploadFile(file, "/images/avatar");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try{
            loginUser.setUserAvatar(cosUploadResult.getFileUrl());
            User updateUserVO = new User();
            BeanUtils.copyProperties(loginUser, updateUserVO);
            this.updateById(updateUserVO);
        }catch (Exception e){
            //删除上传的文件
            cosManager.deleteFile(cosUploadResult.getFileUrl());
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        //TODO: 添加从网络上下载图片的逻辑

        return cosUploadResult.getFileUrl();
    }


}
