package edu.travels.travelapp.model.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ImageItemDTO {
    private String imageId;
    private String userId;
    
    @SerializedName(value = "nickname", alternate = {"nickName", "userNickname", "userNickName"})
    private String nickname;  // 用户昵称（兼容旧字段）
    
    @SerializedName(value = "uploaderNickname", alternate = {"uploaderNickName", "uploader_name"})
    private String uploaderNickname;  // 上传者昵称
    
    @SerializedName(value = "uploaderAvatarUrl", alternate = {"uploaderAvatar", "uploader_avatar"})
    private String uploaderAvatarUrl;  // 上传者头像URL
    
    @SerializedName(value = "userAvatarUrl", alternate = {"avatarUrl", "userAvatar", "avatar"})
    private String userAvatarUrl;  // 用户头像URL（兼容旧字段）
    private String imageUrl;
    private String description;
    private Double gpsLat;
    private Double gpsLng;
    private String locationName;
    private Integer likeCount;
    private Integer commentCount;
    private Date createTime;
    private Boolean liked;
    private Boolean collected;

    public ImageItemDTO() {}

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

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

    public String getUploaderNickname() {
        return uploaderNickname;
    }

    public void setUploaderNickname(String uploaderNickname) {
        this.uploaderNickname = uploaderNickname;
    }

    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }

    public void setUploaderAvatarUrl(String uploaderAvatarUrl) {
        this.uploaderAvatarUrl = uploaderAvatarUrl;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(Double gpsLat) {
        this.gpsLat = gpsLat;
    }

    public Double getGpsLng() {
        return gpsLng;
    }

    public void setGpsLng(Double gpsLng) {
        this.gpsLng = gpsLng;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Boolean getCollected() {
        return collected;
    }

    public void setCollected(Boolean collected) {
        this.collected = collected;
    }
}