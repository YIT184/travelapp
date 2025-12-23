package org.example.travel.mapper;

import org.example.travel.model.entity.Like;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

public interface LikeMapper {
    // 按用户ID和图片ID查询点赞
    @Select("SELECT * FROM `like` WHERE user_id = #{userId} AND image_id = #{imageId}")
    Like selectByUserIdAndImageId(@Param("userId") String userId, @Param("imageId") String imageId);

    // 新增点赞
    @Insert("INSERT INTO `like` (like_id, user_id, image_id, like_time) " +
            "VALUES (#{likeId}, #{userId}, #{imageId}, #{likeTime})")
    void insert(Like like);

    // 按ID删除点赞
    @Delete("DELETE FROM `like` WHERE like_id = #{likeId}")
    void deleteById(String likeId);
}