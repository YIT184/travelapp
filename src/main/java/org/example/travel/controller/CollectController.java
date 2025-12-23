package org.example.travel.controller;

import org.example.travel.model.vo.ResultVO;
import org.example.travel.service.CollectService;
import org.example.travel.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/image")
public class CollectController {
    @Resource
    private CollectService collectService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/collect")
    public ResultVO<?> toggleCollect(@RequestHeader("token") String token, @RequestParam("imageId") String imageId) {
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        boolean isCollect = collectService.toggleCollect(userId, imageId);
        return ResultVO.success(isCollect ? "收藏成功" : "取消收藏成功");
    }
}