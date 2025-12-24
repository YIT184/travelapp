package org.example.travel.controller;

import org.example.travel.model.entity.TravelImage;
import org.example.travel.model.vo.ResultVO;
import org.example.travel.service.ImageService;
import org.example.travel.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/image") // 统一接口前缀
public class ImageController {

    @Resource
    private ImageService imageService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 图片上传：解析真实user_id
     */
    @PostMapping("/upload")
    public ResultVO<String> uploadImage(
            @RequestHeader("token") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "gpsLat", required = false) Double gpsLat,
            @RequestParam(value = "gpsLng", required = false) Double gpsLng,
            @RequestParam(value = "locationName", required = false) String locationName) {
        // 解析Token中的真实user_id
        String userId = jwtUtil.extractUserId(token);

        // 上传到OSS
        String imageUrl = imageService.uploadToOss(file);
        // 保存到数据库
        TravelImage image = new TravelImage();
        image.setImageId(UUID.randomUUID().toString().replace("-", ""));
        image.setUserId(userId); // 关联用户
        image.setImageUrl(imageUrl);
        image.setDescription(description);
        image.setGpsLat(gpsLat);
        image.setGpsLng(gpsLng);
        image.setLocationName(locationName);
        imageService.saveImage(image);
        return ResultVO.success("上传成功", image.getImageId());
    }

    /**
     * 图片列表查询
     */
    @GetMapping("/list")
    public ResultVO<List<TravelImage>> getImageList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        List<TravelImage> imageList = imageService.getImageList(page, size);
        return ResultVO.success("查询成功", imageList);
    }

    /**
     * 更新图片信息
     */
    @PutMapping
    public ResultVO<?> updateImage(
            @RequestParam("imageId") String imageId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "locationName", required = false) String locationName) {
        TravelImage image = imageService.getImageById(imageId);
        if (image == null) {
            return ResultVO.error(404, "图片不存在");
        }
        image.setDescription(description);
        image.setLocationName(locationName);
        imageService.updateImage(image);
        return ResultVO.success("更新成功");
    }

    /**
     * 删除图片
     */
    @DeleteMapping
    public ResultVO<?> deleteImage(@RequestParam("imageId") String imageId) {
        TravelImage image = imageService.getImageById(imageId);
        if (image == null) {
            return ResultVO.error(404, "图片不存在");
        }
        // 先删除OSS文件
        imageService.deleteFromOss(image.getImageUrl());
        // 再删除数据库记录
        imageService.deleteImage(imageId);
        return ResultVO.success("删除成功");
    }
}