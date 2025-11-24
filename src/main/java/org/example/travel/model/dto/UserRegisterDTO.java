package org.example.travel.model.dto;


import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册接口请求参数DTO
 */
@Data
public class UserRegisterDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;          // 手机号

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度必须为6-16位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$", message = "密码必须包含字母和数字")
    private String password;       // 密码

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度必须为2-20位")
    private String nickname;       // 昵称
}