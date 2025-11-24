package org.example.travel.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private String userId;         // 用户唯一标识（对应数据库user_id）
    private String nickname;       // 昵称
    private String avatarUrl;      // 头像URL（对应avatar_url）
    private String phone;          // 手机号
    private String password;       // 加密后密码
    private String signature;      // 个性签名
    private Date createTime;       // 注册时间（对应create_time）
}