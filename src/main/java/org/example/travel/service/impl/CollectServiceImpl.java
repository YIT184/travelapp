package org.example.travel.service.impl;

import jakarta.annotation.Resource;
import org.example.travel.model.entity.Collect;
import org.example.travel.mapper.CollectMapper;
import org.example.travel.exception.BusinessException;
import org.example.travel.service.CollectService; // 1. 新增：导入接口
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.UUID;

/**
 * 收藏服务层：负责收藏/取消收藏逻辑，保障数据一致性
 * 关键修改：1. 类名改为CollectServiceImpl  2. 实现CollectService接口
 */
@Service // 保留@Service注解，让Spring识别为Bean
public class CollectServiceImpl implements CollectService {

    @Resource
    private CollectMapper collectMapper;

    /**
     * 切换收藏/取消收藏状态（幂等性设计）
     * 保留原有业务逻辑，只需保证方法签名和接口一致
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(String userId, String imageId) {

        if (userId == null || imageId == null) {
            throw new BusinessException("用户ID或图片ID不能为空");
        }

        Collect existCollect = collectMapper.selectByUserIdAndImageId(userId, imageId);

        if (existCollect != null) {
            int deleteCount = collectMapper.deleteById(existCollect.getCollectId());
            if (deleteCount == 0) {
                throw new BusinessException("取消收藏失败，请重试");
            }
            return false;
        } else {
            Collect collect = new Collect();
            collect.setCollectId(UUID.randomUUID().toString().replace("-", ""));
            collect.setUserId(userId);
            collect.setImageId(imageId);
            collect.setCollectTime(new Date());
            try {
                collectMapper.insert(collect);
            } catch (Exception e) {
                if (e.getMessage().contains("uk_user_image")) {
                    return true;
                }
                throw new BusinessException("收藏失败：" + e.getMessage());
            }
            return true;
        }
    }
}