package org.com.timess.retrochat.controller.user;

import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.com.timess.retrochat.common.BaseResponse;
import org.com.timess.retrochat.common.ResultUtils;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.exception.ThrowUtils;
import org.com.timess.retrochat.model.entity.user.Friend;
import org.com.timess.retrochat.model.vo.UserMessageVO;
import org.com.timess.retrochat.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * 好友关系表 控制层。
 *
 * @author eternal
 */
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public BaseResponse<List<UserMessageVO>> getFriendList(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        List<UserMessageVO> friendList = friendService.getFriendList(request);
        return ResultUtils.success(friendList);
    }
}
