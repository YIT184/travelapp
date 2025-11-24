package org.example.travel.model.vo;


import lombok.Data;

/**
 * 登录成功后返回给前端的数据
 */
@Data
public class UserLoginVO {
    private String userId;         // 用户ID
    private String token;          // JWT token
    private String nickname;       // 昵称
    private String avatarUrl;      // 头像URL
}