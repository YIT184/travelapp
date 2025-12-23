package org.example.travel.service.impl;

import org.example.travel.model.entity.Like;
import org.example.travel.mapper.LikeMapper;
import org.example.travel.mapper.ImageMapper;
import org.example.travel.exception.BusinessException;
import org.example.travel.service.LikeService; // 1. 导入LikeService接口
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * 点赞服务层：负责点赞/取消点赞逻辑，关联图片表点赞数更新
 * 关键修改：1. 类名改为LikeServiceImpl  2. implements LikeService接口
 */
@Service // 保留@Service注解
public class LikeServiceImpl implements LikeService {

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private ImageMapper imageMapper;

    /**
     * 切换点赞/取消点赞状态（幂等性设计）

     */
    @Override // 必须加，验证方法签名和接口匹配
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(String userId, String imageId) {

        if (userId == null || imageId == null) {
            throw new BusinessException("用户ID或图片ID不能为空");
        }

        if (imageMapper.selectById(imageId) == null) {
            throw new BusinessException("点赞的图片不存在");
        }

        Like existLike = likeMapper.selectByUserIdAndImageId(userId, imageId);

        if (existLike != null) {
            likeMapper.deleteById(existLike.getLikeId());
            imageMapper.decrementLikeCount(imageId);
            return false;
        } else {
            Like like = new Like();
            like.setLikeId(UUID.randomUUID().toString().replace("-", ""));
            like.setUserId(userId);
            like.setImageId(imageId);
            like.setLikeTime(new Date());
            try {
                likeMapper.insert(like);
            } catch (Exception e) {
                if (e.getMessage().contains("uk_user_image")) {
                    return true;
                }
                throw new BusinessException("点赞失败：" + e.getMessage());
            }
            imageMapper.incrementLikeCount(imageId);
            return true;
        }
    }
}