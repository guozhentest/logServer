package com.leantech.admin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.admin.system.entity.SysMenu;
import com.leantech.admin.system.entity.SysRoleMenu;
import com.leantech.admin.system.mapper.SysMenuMapper;
import com.leantech.admin.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public List<SysMenu> selectMenuList(SysMenu menu) {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .like(menu.getMenuName() != null, SysMenu::getMenuName, menu.getMenuName())
                .eq(menu.getStatus() != null, SysMenu::getStatus, menu.getStatus())
                .orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum));
    }

    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, "0")
                .eq(SysMenu::getVisible, "0")
                .orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum));
    }

    public Set<String> selectMenuPermsByUserId(Long userId) {
        return selectMenuTreeByUserId(userId).stream()
                .map(SysMenu::getPerms)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> returnList = new ArrayList<>();
        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() != 0)
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                buildChildren(menu, childrenMap);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) returnList = menus;
        return returnList;
    }

    private void buildChildren(SysMenu parent, Map<Long, List<SysMenu>> childrenMap) {
        List<SysMenu> children = childrenMap.get(parent.getMenuId());
        if (children != null) {
            parent.setChildren(children);
            for (SysMenu child : children) {
                buildChildren(child, childrenMap);
            }
        }
    }

    public SysMenu selectMenuById(Long menuId) {
        return menuMapper.selectById(menuId);
    }

    public int insertMenu(SysMenu menu) {
        return menuMapper.insert(menu);
    }

    public int updateMenu(SysMenu menu) {
        return menuMapper.updateById(menu);
    }

    public boolean hasChildByMenuId(Long menuId) {
        return menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId)) > 0;
    }

    public int deleteMenuById(Long menuId) {
        return menuMapper.deleteById(menuId);
    }
}
