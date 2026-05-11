package com.leantech.admin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leantech.admin.common.PageQuery;
import com.leantech.admin.common.ServiceException;
import com.leantech.admin.common.TableDataInfo;
import com.leantech.admin.system.entity.SysUser;
import com.leantech.admin.system.entity.SysUserRole;
import com.leantech.admin.system.mapper.SysUserMapper;
import com.leantech.admin.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public void updateByIdAfterLogin(Long userId) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setLoginIp("127.0.0.1");
        user.setLoginDate(LocalDateTime.now());
        userMapper.updateById(user);
    }

    public TableDataInfo<SysUser> selectPageUserList(SysUser user, PageQuery pageQuery) {
        Page<SysUser> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(user.getUserName()), SysUser::getUserName, user.getUserName())
               .eq(StringUtils.hasText(user.getStatus()), SysUser::getStatus, user.getStatus())
               .eq(StringUtils.hasText(user.getPhonenumber()), SysUser::getPhonenumber, user.getPhonenumber())
               .eq(SysUser::getDelFlag, "0")
               .orderByAsc(SysUser::getUserId);
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        return TableDataInfo.build(result.getRecords(), result.getTotal());
    }

    public SysUser selectUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    public SysUser selectUserByUserName(String userName) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .eq(SysUser::getDelFlag, "0"));
    }

    public boolean checkUserNameUnique(SysUser user) {
        Long userId = user.getUserId() == null ? -1L : user.getUserId();
        SysUser exist = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, user.getUserName())
                .eq(SysUser::getDelFlag, "0"));
        return exist == null || exist.getUserId().equals(userId);
    }

    @Transactional
    public int insertUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDelFlag("0");
        user.setStatus("0");
        return userMapper.insert(user);
    }

    @Transactional
    public int updateUser(SysUser user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        return userMapper.updateById(user);
    }

    @Transactional
    public int deleteUserByIds(Long[] userIds) {
        return userMapper.deleteBatchIds(Arrays.asList(userIds));
    }

    @Transactional
    public int updateUserStatus(Long userId, String status) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(status);
        return userMapper.updateById(user);
    }

    @Transactional
    public int resetUserPwd(Long userId, String password) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(password));
        return userMapper.updateById(user);
    }

    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    public void updateUserAuth(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    public List<Long> selectRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }
}
