package org.com.timess.retrochat.controller;

import org.com.timess.retrochat.common.BaseResponse;
import org.com.timess.retrochat.common.ResultUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @RequestMapping("/test")
    public BaseResponse<String> test() {
        return ResultUtils.success("Retrochat is running!");
    }
}
