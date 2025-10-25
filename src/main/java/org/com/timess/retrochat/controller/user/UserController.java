package org.com.timess.retrochat.controller.user;

import cn.hutool.core.util.ObjUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.com.timess.retrochat.common.BaseResponse;
import org.com.timess.retrochat.common.ResultUtils;
import org.com.timess.retrochat.exception.BusinessException;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.exception.ThrowUtils;
import org.com.timess.retrochat.model.dto.user.UserAddRequest;
import org.com.timess.retrochat.model.dto.user.UserLoginRequest;
import org.com.timess.retrochat.model.dto.user.UserSendRegisterMailRequest;
import org.com.timess.retrochat.service.UserService;
import org.com.timess.retrochat.utils.EmailApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  控制层。
 *
 * @author eternal
 */
@RestController
@RequestMapping("/user")
public class UserController {


    @Resource
    private UserService userService;

    @Resource
    EmailApi emailApi;
    /**
     * 用户注册
     * @param userAddRequest 注册登录请求类
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<String> userRegister(@RequestBody UserAddRequest userAddRequest){
        userService.userRegister(userAddRequest.getUsername(), userAddRequest.getPassword(),userAddRequest.getEmail(), userAddRequest.getVerifyCode());
        return ResultUtils.success("注册成功");
    }

    /**
     * 用户登录
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        String token = userService.userLogin(userLoginRequest);
        return ResultUtils.success(token);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request){
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Boolean result = userService.logout(request);
        return ResultUtils.success(result);
    }


    @PostMapping("/verifyCode")
    public BaseResponse<Boolean> sendVerifyMail(@RequestBody UserSendRegisterMailRequest mailRequest){
        if(ObjUtil.isEmpty(mailRequest) || StringUtils.isEmpty(mailRequest.getEmail())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入参数错误");
        }
        boolean result = emailApi.sendGeneralEmail("retroChat注册验证码：", mailRequest.getEmail());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "验证码发送失败");
        return ResultUtils.success(true);
    }
}
