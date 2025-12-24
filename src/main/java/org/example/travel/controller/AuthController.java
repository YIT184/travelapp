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

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResultVO<Void> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return ResultVO.success();
    }

    /**
     * 登录接口：生成存储user_id的Token（关键修改）
     */
    @PostMapping("/login")
    public ResultVO<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserLoginVO loginVO = userService.login(loginDTO);
        // 补充：从loginVO或User表中获取真实user_id
        User user = userService.getUserByPhone(loginDTO.getPhone());
        // 生成Token：传入真实user_id（而非手机号）
        String token = jwtUtil.generateToken(user.getUserId());
        // 把Token设置到loginVO中返回
        loginVO.setToken(token);
        return ResultVO.success(loginVO);
    }

    /**
     * 编辑用户资料接口：兼容原有逻辑（解析手机号）
     */
    @PutMapping("/update-user-info")
    public ResultVO<Void> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        String token = authHeader.substring(7);
        String phone = jwtUtil.extractPhone(token); // 兼容旧逻辑
        User user = userService.getUserByPhone(phone);
        userService.updateUserInfo(user.getUserId(), updateDTO);
        return ResultVO.success();
    }
}