package com.leantech.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.admin.system.entity.SysUser;
import com.leantech.admin.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, username)
                .eq(SysUser::getStatus, "0"));
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return User.withUsername(sysUser.getUserName())
                .password(sysUser.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }
}
