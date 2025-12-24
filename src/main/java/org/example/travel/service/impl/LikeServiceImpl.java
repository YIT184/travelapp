package org.example.travel.service.impl;

import org.example.travel.mapper.LikeMapper;
import org.example.travel.model.entity.Like;
import org.example.travel.service.LikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@Service
public class LikeServiceImpl implements LikeService {

    @Resource
    private LikeMapper likeMapper;

    @Override
    @Transactional
    public boolean toggleLike(String userId, String imageId) {
        // 查询是否已点赞
        Like existingLike = likeMapper.selectByUserIdAndImageId(userId, imageId);

        if (existingLike != null) {
            // 已点赞，执行取消点赞
            likeMapper.deleteById(existingLike.getLikeId());
            return false;
        } else {
            // 未点赞，执行点赞
            Like like = new Like();
            like.setLikeId(UUID.randomUUID().toString().replace("-", ""));
            like.setUserId(userId);
            like.setImageId(imageId);
            like.setLikeTime(new Date());
            likeMapper.insert(like);
            return true;
        }
    }
}