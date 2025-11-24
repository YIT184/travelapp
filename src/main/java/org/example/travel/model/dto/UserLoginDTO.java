package org.example.travel.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 登录接口请求参数DTO
 */
@Data
public class UserLoginDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;          // 手机号

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度必须为6-16位")
    private String password;       // 密码
}