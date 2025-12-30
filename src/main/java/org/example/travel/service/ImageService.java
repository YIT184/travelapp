package org.example.travel.service;

import org.example.travel.model.entity.TravelImage;
import org.example.travel.model.vo.TravelImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    // 已有的方法...
    String uploadToOss(MultipartFile file);

    void saveImage(TravelImage image);

    TravelImage getImageById(String imageId);

    void updateImage(TravelImage image);

    void deleteImage(String imageId);

    void deleteFromOss(String imageUrl);

    // 新增方法
    List<TravelImage> getImageList(Integer page, Integer size);
}