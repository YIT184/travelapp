package org.example.travel.mapper;

import org.apache.ibatis.annotations.*;
import org.example.travel.model.entity.TravelImage;

import java.util.List;

public interface ImageMapper {

    @Insert("INSERT INTO travel_image (image_id, user_id, image_url, description, gps_lat, gps_lng, location_name, create_time) " +
            "VALUES (#{imageId}, #{userId}, #{imageUrl}, #{description}, #{gpsLat}, #{gpsLng}, #{locationName}, NOW())")
    void insert(TravelImage image);

    @Select("SELECT * FROM travel_image WHERE image_id = #{imageId}")
    TravelImage selectById(String imageId);

    @Update("UPDATE travel_image SET description = #{description}, gps_lat = #{gpsLat}, " +
            "gps_lng = #{gpsLng}, location_name = #{locationName} WHERE image_id = #{imageId}")
    void updateById(TravelImage image);

    @Delete("DELETE FROM travel_image WHERE image_id = #{imageId}")
    void deleteById(String imageId);
    // 新增分页查询方法
    @Select("SELECT ti.*, u.nickname as uploader_nickname, u.avatar_url as uploader_avatar_url " +
            "FROM travel_image ti " +
            "LEFT JOIN user u ON ti.user_id = u.user_id " +
            "ORDER BY ti.create_time DESC LIMIT #{offset}, #{size}")
    List<TravelImage> selectImageList(@Param("offset") int offset, @Param("size") int size);
}