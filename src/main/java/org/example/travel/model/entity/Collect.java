package org.example.travel.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Collect {
    private String collectId;
    private String userId;
    private String imageId;
    private Date collectTime;
}
