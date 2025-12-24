package org.example.travel.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TravelImage {
    private String imageId;
    private String userId;
    private String imageUrl;
    private String description;
    private Double gpsLat;
    private Double gpsLng;
    private String locationName;
    private Date createTime;
}