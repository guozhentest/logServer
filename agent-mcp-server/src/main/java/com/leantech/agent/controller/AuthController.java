package com.leantech.agent.controller;

import com.leantech.agent.model.ApiResult;
import com.leantech.agent.model.LoginRequest;
import com.leantech.agent.model.LoginResponse;
import com.leantech.agent.service.UserService;
import com.leantech.agent.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AuthController extends BaseController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@RequestBody LoginRequest request) {
        if (userService.validateUser(request.getUsername(), request.getPassword())) {
            var user = userService.getUser(request.getUsername());
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            return ok(new LoginResponse(token, user.getRole()));
        }
        return fail(401, "用户名或密码错误");
    }
}