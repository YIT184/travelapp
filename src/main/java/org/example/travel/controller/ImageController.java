package org.example.travel.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.travel.model.entity.TravelImage;
import org.example.travel.model.vo.ResultVO;
import org.example.travel.service.ImageService;
import org.example.travel.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.util.UUID;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Resource
    private ImageService imageService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 图片上传
     */
    @PostMapping("/upload")
    public ResultVO<String> uploadImage(
            @RequestHeader("token") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "gpsLat", required = false) Double gpsLat,
            @RequestParam(value = "gpsLng", required = false) Double gpsLng,
            @RequestParam(value = "locationName", required = false) String locationName) {
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        // 上传到OSS
        String imageUrl = imageService.uploadToOss(file);
        // 保存到数据库
        TravelImage image = new TravelImage();
        image.setImageId(UUID.randomUUID().toString().replace("-", ""));
        image.setUserId(userId);
        image.setImageUrl(imageUrl);
        image.setDescription(description);
        image.setGpsLat(gpsLat);
        image.setGpsLng(gpsLng);
        image.setLocationName(locationName);
        imageService.saveImage(image);
        return ResultVO.success("上传成功", image.getImageId());
    }

    /**
     * 图片列表查询（分页）
     */
    @GetMapping("/list")
    public ResultVO<PageInfo<TravelImage>> getImageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<TravelImage> pageInfo = new PageInfo<>(imageService.getImageList());
        return ResultVO.success(pageInfo);
    }
}