package com.leantech.admin.system.controller;

import com.leantech.admin.common.*;
import com.leantech.admin.system.entity.SysMenu;
import com.leantech.admin.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController extends BaseController {

    private final SysMenuService menuService;

    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu);
        return R.ok(menuService.buildMenuTree(menus));
    }

    @GetMapping("/treeselect")
    public R<List<SysMenu>> treeselect(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu);
        return R.ok(menuService.buildMenuTree(menus));
    }

    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable Long menuId) {
        return R.ok(menuService.selectMenuById(menuId));
    }

    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        menu.setStatus("0");
        menuService.insertMenu(menu);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return R.ok();
    }

    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return R.fail("存在子菜单，不允许删除");
        }
        menuService.deleteMenuById(menuId);
        return R.ok();
    }
}
