package org.example.travel.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.travel.model.entity.User;

@Mapper
public interface UserMapper {

    // 基础CRUD方法需要手动定义
    User selectById(@Param("userId") String userId);
    int insert(User user);
    int update(User user);
    void deleteById(@Param("userId") String userId);

    // 自定义方法
    User selectByPhone(@Param("phone") String phone);
}