package org.example.travel.service.impl;

import java.util.Arrays;
import java.util.List;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.example.travel.model.entity.TravelImage;
import org.example.travel.mapper.ImageMapper;
import org.example.travel.exception.BusinessException;
import org.example.travel.service.ImageService;
import org.example.travel.service.UserService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;


/**
 * 图片服务层：负责图片上传OSS、数据入库、列表查询、点赞数更新等逻辑
 * 关键修改：1. 类名改为ImageServiceImpl  2. 实现ImageService接口
 */
/**
 * 图片服务实现类
 */
@Service
public class ImageServiceImpl implements ImageService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private UserService userService;

    /**
     * 上传图片到阿里云OSS
     */
    @Override
    public String uploadToOss(MultipartFile file) {
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
    public void saveImage(TravelImage image) {
        imageMapper.insert(image);
    }

    /**
     * 根据ID查询图片
     */
    @Override
    public TravelImage getImageById(String imageId) {
        return imageMapper.selectById(imageId);
    }

    /**
     * 更新图片信息
     */
    @Override
    public void updateImage(TravelImage image) {
        imageMapper.updateById(image);
    }

    /**
     * 从数据库删除图片记录
     */
    @Override
    public void deleteImage(String imageId) {
        imageMapper.deleteById(imageId);
    }

    /**
     * 从OSS删除图片文件
     */
    @Override
    public void deleteFromOss(String imageUrl) {
        // 解析URL获取文件名
        String fileName = imageUrl.substring(imageUrl.indexOf(bucketName + "." + endpoint + "/") + (bucketName + "." + endpoint + "/").length());

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            if (ossClient.doesObjectExist(bucketName, fileName)) {
                ossClient.deleteObject(bucketName, fileName);
            }
        } catch (Exception e) {
            throw new BusinessException("从OSS删除图片失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传的图片为空");
        }

        // 验证文件大小，这里限制10MB
        long fileSize = file.getSize();
        if (fileSize > 10 * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过10MB");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String fileSuffix = FilenameUtils.getExtension(originalFilename).toLowerCase();
        List<String> allowedSuffix = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
        if (!allowedSuffix.contains(fileSuffix)) {
            throw new BusinessException("不支持的图片格式，仅支持jpg、jpeg、png、gif、bmp");
        }
    }

    @Override
    public List<TravelImage> getImageList(Integer page, Integer size) {
        int offset = (page - 1) * size;
        return imageMapper.selectImageList(offset, size);
    }


}