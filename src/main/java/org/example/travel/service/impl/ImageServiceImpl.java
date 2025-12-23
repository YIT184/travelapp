package org.example.travel.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.example.travel.model.entity.TravelImage;
import org.example.travel.mapper.ImageMapper;
import org.example.travel.exception.BusinessException;
import org.example.travel.service.ImageService; // 1. 新增：导入接口
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 图片服务层：负责图片上传OSS、数据入库、列表查询、点赞数更新等逻辑
 * 关键修改：1. 类名改为ImageServiceImpl  2. 实现ImageService接口
 */
@Service // 保留@Service注解
public class ImageServiceImpl implements ImageService {

    // 阿里云OSS配置（从application.yml读取）
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    // 允许上传的图片格式
    private static final String[] ALLOWED_IMAGE_FORMATS = {"jpg", "jpeg", "png", "gif", "webp"};
    // 图片最大大小（10MB）
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    @Resource
    private ImageMapper imageMapper;

    /**
     * 上传图片到阿里云OSS

     */
    @Override
    public String uploadToOss(MultipartFile file) {
        // 原有逻辑完全保留，无需修改
        validateImageFile(file);
        String originalFilename = file.getOriginalFilename();
        String fileSuffix = FilenameUtils.getExtension(originalFilename);
        String fileName = "travel/" + UUID.randomUUID().toString().replace("-", "") + "." + fileSuffix;

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream());
            ossClient.putObject(putObjectRequest);
            return String.format("https://%s.%s/%s", bucketName, endpoint, fileName);
        } catch (IOException e) {
            throw new BusinessException("图片上传OSS失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 保存图片信息到数据库

     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveImage(TravelImage travelImage) {
        // 原有逻辑完全保留
        travelImage.setLikeCount(travelImage.getLikeCount() == null ? 0 : travelImage.getLikeCount());
        travelImage.setCommentCount(travelImage.getCommentCount() == null ? 0 : travelImage.getCommentCount());
        travelImage.setCreateTime(new Date());
        imageMapper.insert(travelImage);
    }

    /**
     * 分页查询图片列表（按创建时间倒序）
     */
    @Override
    public List<TravelImage> getImageList() {
        // 原有逻辑完全保留
        return imageMapper.selectAllByCreateTimeDesc();
    }

    /**
     * 图片点赞数+1
     * ✅ 新增@Override注解
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementLikeCount(String imageId) {
        // 原有逻辑完全保留
        TravelImage image = imageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException("图片不存在，点赞失败");
        }
        imageMapper.incrementLikeCount(imageId);
    }

    /**
     * 图片点赞数-1（防止负数）
     * ✅ 新增@Override注解
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementLikeCount(String imageId) {
        // 原有逻辑完全保留
        TravelImage image = imageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException("图片不存在，取消点赞失败");
        }
        if (image.getLikeCount() > 0) {
            imageMapper.decrementLikeCount(imageId);
        }
    }

    /**
     * 图片文件合法性校验（格式+大小）
     */
    private void validateImageFile(MultipartFile file) {
        // 原有逻辑完全保留
        if (file.isEmpty()) {
            throw new BusinessException("上传图片不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("图片大小不能超过10MB");
        }
        String originalFilename = file.getOriginalFilename();
        String fileSuffix = FilenameUtils.getExtension(originalFilename).toLowerCase();
        boolean isAllowed = false;
        for (String format : ALLOWED_IMAGE_FORMATS) {
            if (format.equals(fileSuffix)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new BusinessException("仅支持jpg、jpeg、png、gif、webp格式图片");
        }
    }
}