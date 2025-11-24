package org.example.travel.controller;


import org.example.travel.model.dto.UserLoginDTO;
import org.example.travel.model.dto.UserRegisterDTO;
import org.example.travel.model.dto.UserUpdateDTO;
import org.example.travel.model.entity.User;
import org.example.travel.model.vo.ResultVO;
import org.example.travel.model.vo.UserLoginVO;
import org.example.travel.service.UserService;
import org.example.travel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户认证接口控制器（登录、注册、编辑资料）
 */
@RestController
@RequestMapping("/auth")  // 接口路径前缀：/api/auth（结合application.yml的context-path）
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册接口
     * 接口地址：POST /api/auth/register
     */
    @PostMapping("/register")
    public ResultVO<Void> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return ResultVO.success();
    }

    /**
     * 用户登录接口
     * 接口地址：POST /api/auth/login
     */
    @PostMapping("/login")
    public ResultVO<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserLoginVO loginVO = userService.login(loginDTO);
        return ResultVO.success(loginVO);
    }

    /**
     * 编辑用户资料接口（需要token认证）
     * 接口地址：PUT /api/auth/update-user-info
     */
    @PutMapping("/update-user-info")
    public ResultVO<Void> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        // 1. 从token中解析手机号，查询用户ID
        String token = authHeader.substring(7);
        String phone = jwtUtil.extractPhone(token);
        User user = userService.getUserByPhone(phone);

        // 2. 调用服务更新资料
        userService.updateUserInfo(user.getUserId(), updateDTO);
        return ResultVO.success();
    }
}