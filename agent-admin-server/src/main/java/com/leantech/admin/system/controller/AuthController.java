package com.leantech.admin.system.controller;

import com.leantech.admin.common.R;
import com.leantech.admin.security.JwtTokenProvider;
import com.leantech.admin.system.entity.SysMenu;
import com.leantech.admin.system.entity.SysRole;
import com.leantech.admin.system.entity.SysUser;
import com.leantech.admin.system.service.SysMenuService;
import com.leantech.admin.system.service.SysRoleService;
import com.leantech.admin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SysUser user = userService.selectUserByUserName(request.getUsername());
        String token = tokenProvider.generateToken(user.getUserName(), user.getUserId());
        userService.updateByIdAfterLogin(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return R.ok(result);
    }

    @DeleteMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }

    @GetMapping("/getInfo")
    public R<Map<String, Object>> getInfo(Authentication authentication) {
        SysUser user = userService.selectUserByUserName(authentication.getName());
        List<Long> roleIds = userService.selectRoleIdsByUserId(user.getUserId());
        List<SysRole> roles = roleService.selectRoleAll();
        Set<String> perms = menuService.selectMenuPermsByUserId(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roles);
        result.put("permissions", perms);
        return R.ok(result);
    }

    @GetMapping("/getRouters")
    public R<List<SysMenu>> getRouters() {
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(1L);
        return R.ok(menuService.buildMenuTree(menus));
    }

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
