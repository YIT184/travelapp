package org.example.travel.service;

import org.example.travel.model.dto.UserLoginDTO;
import org.example.travel.model.dto.UserRegisterDTO;
import org.example.travel.model.dto.UserUpdateDTO;
import org.example.travel.model.entity.User;
import org.example.travel.model.vo.UserLoginVO;

/**
 * 用户服务接口
 */
public interface UserService {
    // 用户注册
    void register(UserRegisterDTO registerDTO);

    // 用户登录（返回登录成功VO）
    UserLoginVO login(UserLoginDTO loginDTO);

    // 编辑用户资料
    void updateUserInfo(String userId, UserUpdateDTO updateDTO);

    // 根据手机号查询用户
    User getUserByPhone(String phone);

    // 根据用户ID查询用户（新增方法）
    User getById(String userId);

}
