package org.example.travel.service;

import org.example.travel.model.entity.TravelImage;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ImageService {
    // 上传图片到OSS
    String uploadToOss(MultipartFile file);

    // 保存图片信息到数据库
    void saveImage(TravelImage travelImage);

    // 查询图片列表
    List<TravelImage> getImageList();

    // 点赞数+1
    void incrementLikeCount(String imageId);

    // 点赞数-1
    void decrementLikeCount(String imageId);
}
