package org.example.travel.model.dto;


import lombok.Data;
import jakarta.validation.constraints.Size;

/**
 * 编辑用户资料接口请求参数DTO
 */
@Data
public class UserUpdateDTO {
    @Size(min = 2, max = 20, message = "昵称长度必须为2-20位")
    private String nickname;       // 昵称（可选）

    @Size(max = 100, message = "个性签名最多100个字符")
    private String signature;      // 个性签名（可选）

    private String avatarUrl;      // 头像URL（可选，从图片上传接口获取）
}