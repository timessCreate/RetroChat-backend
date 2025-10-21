package org.com.timess.retrochat.controller;

import com.mybatisflex.core.paginate.Page;
import org.com.timess.retrochat.common.BaseResponse;
import org.com.timess.retrochat.common.ResultUtils;
import org.com.timess.retrochat.model.entity.LoginLog;
import org.com.timess.retrochat.service.LoginLogService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;
import java.util.List;

/**
 *  控制层。
 *
 * @author eternal
 */
@RestController
@RequestMapping("/loginLog")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    /**
     * 保存。
     *
     * @param loginLog 
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public BaseResponse<Boolean> save(@RequestBody LoginLog loginLog) {
        return ResultUtils.success(loginLogService.save(loginLog));
    }

    /**
     * 根据主键删除。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public  BaseResponse<Boolean> remove(@PathVariable Long id) {
        return ResultUtils.success(loginLogService.removeById(id));
    }

    /**
     * 根据主键更新。
     *
     * @param loginLog 
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public  BaseResponse<Boolean> update(@RequestBody LoginLog loginLog) {
        return ResultUtils.success(loginLogService.updateById(loginLog));
    }

    /**
     * 查询所有。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public BaseResponse<List<LoginLog>> list() {
        return ResultUtils.success(loginLogService.list());
    }

    /**
     * 根据主键获取。
     *
     * @param id 主键
     * @return 详情
     */
    @GetMapping("getInfo/{id}")
    public BaseResponse<LoginLog> getInfo(@PathVariable Long id) {
        return ResultUtils.success(loginLogService.getById(id));
    }

    /**
     * 分页查询。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public BaseResponse<Page<LoginLog>> page(Page<LoginLog> page) {
        return ResultUtils.success(loginLogService.page(page));
    }

}
