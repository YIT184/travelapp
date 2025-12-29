package edu.travels.travelapp.model.dto;

public class UserUpdateDTO {
    private String nickname;
    private String password;    // 为空字符串表示不修改
    private String signature;   // 为空字符串表示不修改或删除
    private String avatarUrl;   // 为空字符串或 null 表示不修改头像

    public UserUpdateDTO() {}

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
