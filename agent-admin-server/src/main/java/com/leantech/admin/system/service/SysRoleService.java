package com.leantech.admin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leantech.admin.common.PageQuery;
import com.leantech.admin.common.TableDataInfo;
import com.leantech.admin.system.entity.SysRole;
import com.leantech.admin.system.entity.SysRoleMenu;
import com.leantech.admin.system.mapper.SysRoleMapper;
import com.leantech.admin.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public TableDataInfo<SysRole> selectPageRoleList(SysRole role, PageQuery pageQuery) {
        Page<SysRole> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(role.getRoleName()), SysRole::getRoleName, role.getRoleName())
               .eq(StringUtils.hasText(role.getStatus()), SysRole::getStatus, role.getStatus())
               .eq(SysRole::getDelFlag, "0")
               .orderByAsc(SysRole::getRoleSort);
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        return TableDataInfo.build(result.getRecords(), result.getTotal());
    }

    public List<SysRole> selectRoleAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDelFlag, "0")
                .eq(SysRole::getStatus, "0")
                .orderByAsc(SysRole::getRoleSort));
    }

    public SysRole selectRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    public boolean checkRoleNameUnique(SysRole role) {
        Long roleId = role.getRoleId() == null ? -1L : role.getRoleId();
        SysRole exist = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleName, role.getRoleName())
                .eq(SysRole::getDelFlag, "0"));
        return exist == null || exist.getRoleId().equals(roleId);
    }

    public boolean checkRoleKeyUnique(SysRole role) {
        Long roleId = role.getRoleId() == null ? -1L : role.getRoleId();
        SysRole exist = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, role.getRoleKey())
                .eq(SysRole::getDelFlag, "0"));
        return exist == null || exist.getRoleId().equals(roleId);
    }

    @Transactional
    public int insertRole(SysRole role) {
        role.setDelFlag("0");
        role.setStatus("0");
        return roleMapper.insert(role);
    }

    @Transactional
    public int updateRole(SysRole role) {
        return roleMapper.updateById(role);
    }

    @Transactional
    public int deleteRoleByIds(Long[] roleIds) {
        return roleMapper.deleteBatchIds(Arrays.asList(roleIds));
    }

    @Transactional
    public int updateRoleStatus(Long roleId, String status) {
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setStatus(status);
        return roleMapper.updateById(role);
    }

    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Transactional
    public void updateRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
