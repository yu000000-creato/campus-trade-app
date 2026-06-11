package com.example.campustrade.controller;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.Result;
import com.example.campustrade.dto.response.ItemResponse;
import com.example.campustrade.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public Result<String> add(@RequestParam Long userId, @RequestParam Long itemId) {
        favoriteService.add(userId, itemId);
        return Result.success("收藏成功");
    }

    @DeleteMapping
    public Result<String> remove(@RequestParam Long userId, @RequestParam Long itemId) {
        favoriteService.remove(userId, itemId);
        return Result.success("取消收藏成功");
    }

    @GetMapping("/user/{userId}")
    public Result<PageResult<ItemResponse>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(favoriteService.list(userId, page, size));
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long userId, @RequestParam Long itemId) {
        return Result.success(favoriteService.isFavorite(userId, itemId));
    }
}