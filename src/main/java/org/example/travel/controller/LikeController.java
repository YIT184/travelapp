package org.example.travel.controller;

import org.example.travel.model.vo.ResultVO;
import org.example.travel.service.LikeService;
import org.example.travel.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/image")
public class LikeController {
    @Resource
    private LikeService likeService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    public ResultVO<?> toggleLike(@RequestHeader("token") String token, @RequestParam("imageId") String imageId) {
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        boolean isLike = likeService.toggleLike(userId, imageId);
        return ResultVO.success(isLike ? "点赞成功" : "取消点赞成功");
    }
}