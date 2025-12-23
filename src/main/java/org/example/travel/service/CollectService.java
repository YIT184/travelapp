package org.example.travel.service;

public interface CollectService {
    // 切换收藏/取消收藏
    boolean toggleCollect(String userId, String imageId);
}