package edu.travels.travelapp.model.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class ImageItemDTO implements Serializable {

    // --- 核心字段：必须与 JSON key 一致 ---

    // 对应 JSON 中的 "uploaderNickname": "YIT"
    @SerializedName("uploaderNickname")
    private String uploaderNickname;

    // 对应 JSON 中的 "uploaderAvatarUrl": "https://..."
    @SerializedName("uploaderAvatarUrl")
    private String uploaderAvatarUrl;

    // --- 其他常用字段 ---

    @SerializedName(value = "imageId", alternate = {"id"})
    private String imageId;

    @SerializedName(value = "imageUrl", alternate = {"url", "image_url"})
    private String imageUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("createTime")
    private Date createTime;

    @SerializedName("locationName")
    private String locationName;

    @SerializedName("userId")
    private String userId;

    @SerializedName("gpsLat")
    private Double gpsLat;

    @SerializedName("gpsLng")
    private Double gpsLng;


    // 兼容旧字段（可选，保留以防万一）
    @SerializedName("nickname")
    private String nickname;

    @SerializedName("userAvatarUrl")
    private String userAvatarUrl;

    // --- 构造函数 ---


    // --- Getter 和 Setter 方法 ---

    public String getUploaderNickname() {
        return uploaderNickname;
    }


    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }


    public String getImageId() {
        return imageId;
    }



    public String getImageUrl() {
        return imageUrl;
    }


    public String getDescription() {
        return description;
    }


    public Date getCreateTime() {
        return createTime;
    }


    public String getLocationName() {
        return locationName;
    }



    public String getUserId() {
        return userId;
    }


    public Double getGpsLat() {
        return gpsLat;
    }


    public Double getGpsLng() {
        return gpsLng;
    }


    public String getNickname() {
        return nickname;
    }


    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

}
