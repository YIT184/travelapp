package org.example.travel.mapper;

import org.example.travel.model.entity.TravelImage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

public interface ImageMapper {
    // 新增图片
    @Insert("INSERT INTO travel_image (image_id, user_id, image_url, description, gps_lat, gps_lng, location_name, like_count, comment_count, create_time) " +
            "VALUES (#{imageId}, #{userId}, #{imageUrl}, #{description}, #{gpsLat}, #{gpsLng}, #{locationName}, #{likeCount}, #{commentCount}, #{createTime})")
    void insert(TravelImage travelImage);

    // 按ID查询图片
    @Select("SELECT * FROM travel_image WHERE image_id = #{imageId}")
    TravelImage selectById(String imageId);

    // 按创建时间倒序查询所有图片
    @Select("SELECT * FROM travel_image ORDER BY create_time DESC")
    List<TravelImage> selectAllByCreateTimeDesc();

    // 点赞数+1
    @Update("UPDATE travel_image SET like_count = like_count + 1 WHERE image_id = #{imageId}")
    void incrementLikeCount(String imageId);

    // 点赞数-1
    @Update("UPDATE travel_image SET like_count = like_count - 1 WHERE image_id = #{imageId} AND like_count > 0")
    void decrementLikeCount(String imageId);
}