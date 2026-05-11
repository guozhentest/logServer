package com.leantech.admin.system.controller;

import com.leantech.admin.common.*;
import com.leantech.admin.system.entity.SysRole;
import com.leantech.admin.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {

    private final SysRoleService roleService;

    @GetMapping("/list")
    public TableDataInfo<SysRole> list(SysRole role, PageQuery pageQuery) {
        return roleService.selectPageRoleList(role, pageQuery);
    }

    @GetMapping("/optionselect")
    public R<List<SysRole>> optionselect() {
        return R.ok(roleService.selectRoleAll());
    }

    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        SysRole role = roleService.selectRoleById(roleId);
        List<Long> menuIds = roleService.selectMenuIdsByRoleId(roleId);
        role.setMenuIds(menuIds);
        return R.ok(role);
    }

    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role)) {
            return R.fail("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        roleService.insertRole(role);
        roleService.updateRoleMenus(role.getRoleId(), role.getMenuIds());
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role)) {
            return R.fail("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        roleService.updateRole(role);
        roleService.updateRoleMenus(role.getRoleId(), role.getMenuIds());
        return R.ok();
    }

    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable Long[] roleIds) {
        roleService.deleteRoleByIds(roleIds);
        return R.ok();
    }

    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        roleService.updateRoleStatus(role.getRoleId(), role.getStatus());
        return R.ok();
    }
}
