package com.leantech.admin.system.controller;

import com.leantech.admin.common.*;
import com.leantech.admin.system.entity.SysUser;
import com.leantech.admin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

    private final SysUserService userService;

    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser user, PageQuery pageQuery) {
        return userService.selectPageUserList(user, pageQuery);
    }

    @GetMapping("/{userId}")
    public R<SysUser> getInfo(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        List<Long> roleIds = userService.selectRoleIdsByUserId(userId);
        user.setRoleIds(roleIds);
        return R.ok(user);
    }

    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        userService.insertUser(user);
        userService.updateUserAuth(user.getUserId(), user.getRoleIds());
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        userService.updateUser(user);
        userService.updateUserAuth(user.getUserId(), user.getRoleIds());
        return R.ok();
    }

    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable Long[] userIds) {
        userService.deleteUserByIds(userIds);
        return R.ok();
    }

    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        userService.updateUserStatus(user.getUserId(), user.getStatus());
        return R.ok();
    }

    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody SysUser user) {
        userService.resetUserPwd(user.getUserId(), user.getPassword());
        return R.ok();
    }
}
