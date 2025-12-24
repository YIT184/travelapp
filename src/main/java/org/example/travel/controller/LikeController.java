package org.example.travel.controller;

import org.example.travel.model.vo.ResultVO;
import org.example.travel.service.LikeService;
import org.example.travel.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 点赞/取消点赞控制器
 */
@RestController
public class LikeController {

    @Resource
    private LikeService likeService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    public ResultVO<?> toggleLike(@RequestHeader("token") String token, @RequestParam("imageId") String imageId) {
        // 解析真实user_id
        String userId = jwtUtil.extractUserId(token);
        boolean isLike = likeService.toggleLike(userId, imageId);
        return ResultVO.success(isLike ? "点赞成功" : "取消点赞成功");
    }
}