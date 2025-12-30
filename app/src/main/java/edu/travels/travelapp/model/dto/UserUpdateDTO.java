package edu.travels.travelapp.model.dto;

import com.google.gson.annotations.SerializedName;

public class UserUpdateDTO {
    @SerializedName(value = "userId", alternate = {"user_id", "id"})
    private String userId;  // 用户ID（后端可能返回）
    
    @SerializedName(value = "nickname", alternate = {"nickName", "userNickname", "userNickName", "name"})
    private String nickname;
    
    @SerializedName(value = "password", alternate = {"pwd"})
    private String password;    // 为空字符串表示不修改（后端返回时通常为null）
    
    @SerializedName(value = "signature", alternate = {"userSignature", "personalSignature", "bio", "description"})
    private String signature;   // 为空字符串表示不修改或删除
    
    @SerializedName(value = "avatarUrl", alternate = {"avatar", "userAvatar", "userAvatarUrl", "avatar_url"})
    private String avatarUrl;   // 为空字符串或 null 表示不修改头像

    public UserUpdateDTO() {}
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

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
