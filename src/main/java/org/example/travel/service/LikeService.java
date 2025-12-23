package org.example.travel.service;

public interface LikeService {
    // 切换点赞/取消点赞
    boolean toggleLike(String userId, String imageId);
}