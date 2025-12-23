package org.example.travel.mapper;

import org.example.travel.model.entity.Collect;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

public interface CollectMapper {
    // 按用户ID和图片ID查询收藏
    @Select("SELECT * FROM collect WHERE user_id = #{userId} AND image_id = #{imageId}")
    Collect selectByUserIdAndImageId(@Param("userId") String userId, @Param("imageId") String imageId);

    // 新增收藏
    @Insert("INSERT INTO collect (collect_id, user_id, image_id, collect_time) " +
            "VALUES (#{collectId}, #{userId}, #{imageId}, #{collectTime})")
    void insert(Collect collect);

    // 按ID删除收藏
    @Delete("DELETE FROM collect WHERE collect_id = #{collectId}")
    int deleteById(String collectId);
}