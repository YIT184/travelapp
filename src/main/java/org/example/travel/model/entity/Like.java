package org.example.travel.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Like {
    private String likeId;
    private String userId;
    private String imageId;
    private Date likeTime;
}