package org.example.travel.service.impl;

import org.example.travel.exception.BusinessException;
import org.example.travel.mapper.UserMapper;
import org.example.travel.model.dto.UserLoginDTO;
import org.example.travel.model.dto.UserRegisterDTO;
import org.example.travel.model.dto.UserUpdateDTO;
import org.example.travel.model.entity.User;
import org.example.travel.model.vo.UserLoginVO;
import org.example.travel.service.UserService;
import org.example.travel.util.JwtUtil;
import org.example.travel.util.MD5Util;
import org.example.travel.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册逻辑
     */
    @Override
    public void register(UserRegisterDTO registerDTO) {
        // 1. 校验手机号是否已注册
        User existingUser = userMapper.selectByPhone(registerDTO.getPhone());
        if (!ObjectUtils.isEmpty(existingUser)) {
            throw new BusinessException("该手机号已注册");
        }

        // 2. 构建User实体
        User user = new User();
        user.setUserId(UUIDUtil.getUUID());  // 生成UUID作为用户ID
        user.setPhone(registerDTO.getPhone());
        user.setPassword(MD5Util.encrypt(registerDTO.getPassword()));  // 密码MD5加密
        user.setNickname(registerDTO.getNickname());
        user.setCreateTime(new Date());  // 注册时间

        // 3. 保存到数据库（使用 userMapper.insert）
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("注册失败，请重试");
        }
    }

    /**
     * 用户登录逻辑
     */
    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        // 1. 校验用户是否存在
        User user = userMapper.selectByPhone(loginDTO.getPhone());
        if (ObjectUtils.isEmpty(user)) {
            throw new BusinessException(401, "账号或密码错误");
        }

        // 2. 校验密码是否正确（前端传入密码加密后与数据库对比）
        String encryptedPassword = MD5Util.encrypt(loginDTO.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }

        // 3. 生成JWT token
        String token = jwtUtil.generateToken(user.getPhone());

        // 4. 构建并返回登录成功VO
        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setUserId(user.getUserId());
        loginVO.setToken(token);
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatarUrl(user.getAvatarUrl());

        return loginVO;
    }

    /**
     * 编辑用户资料逻辑
     */
    @Override
    public void updateUserInfo(String userId, UserUpdateDTO updateDTO) {
        // 1. 校验用户是否存在（使用 userMapper.selectById）
        User user = userMapper.selectById(userId);
        if (ObjectUtils.isEmpty(user)) {
            throw new BusinessException("用户不存在");
        }

        // 2. 更新字段（只更新传入的非空字段）
        if (!ObjectUtils.isEmpty(updateDTO.getNickname())) {
            user.setNickname(updateDTO.getNickname());
        }
        if (!ObjectUtils.isEmpty(updateDTO.getSignature())) {
            user.setSignature(updateDTO.getSignature());
        }
        if (!ObjectUtils.isEmpty(updateDTO.getAvatarUrl())) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }

        // 3. 保存更新（使用 userMapper.update）
        int result = userMapper.update(user);
        if (result <= 0) {
            throw new BusinessException("资料修改失败，请重试");
        }
    }

    /**
     * 根据手机号查询用户
     */
    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    /**
     * 根据用户ID查询用户（新增方法）
     */
    public User getById(String userId) {
        return userMapper.selectById(userId);
    }
}