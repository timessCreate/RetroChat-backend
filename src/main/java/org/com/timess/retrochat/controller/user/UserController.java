package org.com.timess.retrochat.controller.user;

import cn.hutool.core.util.ObjUtil;
import jakarta.annotation.Resource;
import jakarta.mail.Multipart;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.com.timess.retrochat.common.BaseResponse;
import org.com.timess.retrochat.common.ResultUtils;
import org.com.timess.retrochat.exception.BusinessException;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.exception.ThrowUtils;
import org.com.timess.retrochat.manager.CosManager;
import org.com.timess.retrochat.model.dto.user.UserAddRequest;
import org.com.timess.retrochat.model.dto.user.UserLoginRequest;
import org.com.timess.retrochat.model.dto.user.UserSendRegisterMailRequest;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.model.vo.UserVO;
import org.com.timess.retrochat.service.UserService;
import org.com.timess.retrochat.utils.EmailApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 *  控制层。
 *
 * @author eternal
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {


    @Resource
    private UserService userService;

    @Resource
    EmailApi emailApi;

    @Resource
    CosManager cosManager;

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
    /**
     * 发送验证码
     * @param mailRequest
     * @return
     */
    @PostMapping("/verifyCode")
    public BaseResponse<Boolean> sendVerifyMail(@RequestBody UserSendRegisterMailRequest mailRequest){
        if(ObjUtil.isEmpty(mailRequest) || StringUtils.isEmpty(mailRequest.getEmail())){
            throw new BusinessException(ErrorCode.VERIFY_CODE_ERROR, "传入参数错误");
        }
        boolean result = emailApi.sendGeneralEmail("retroChat注册验证码", mailRequest.getEmail());
        ThrowUtils.throwIf(!result, ErrorCode.VERIFY_CODE_ERROR, "验证码发送失败");
        return ResultUtils.success(true);
    }


    @PostMapping("/profile/update")
    public BaseResponse<UserVO> updateProfile(@RequestBody UserVO userVO, HttpServletRequest request){
        try{
            UserVO result = userService.updateProfile(userVO, request);
            return ResultUtils.success(result);
        }catch (Exception e){
            log.error("修改用户信息失败：" + e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改用户信息失败，请联系管理员");
        }
    }

    @PostMapping("/avatar/update")
    public BaseResponse<String> updateAvatar(MultipartFile file, HttpServletRequest request){
        try{
            String result = userService.updateAvatar(file, request);
            return ResultUtils.success(result);
        }catch (Exception e){
            log.error("修改用户头像失败：" + e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改用户头像失败，请联系管理员");
        }
    }


}
