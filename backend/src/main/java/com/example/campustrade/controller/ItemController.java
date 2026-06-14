package com.example.campustrade.controller;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.Result;
import com.example.campustrade.dto.request.ItemCreateRequest;
import com.example.campustrade.dto.response.ItemResponse;
import com.example.campustrade.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public Result<ItemResponse> create(@RequestParam Long userId, @Valid @RequestBody ItemCreateRequest request) {
        return Result.success(itemService.create(userId, request));
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadImage(@RequestParam("image") MultipartFile file) {
        String imageUrl = itemService.uploadImage(file);
        return Result.success("图片上传成功", imageUrl);
    }

    @GetMapping("/{id}")
    public Result<ItemResponse> getById(@PathVariable Long id) {
        return Result.success(itemService.getById(id));
    }

    @GetMapping
    public Result<PageResult<ItemResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "time_desc") String sort) {
        return Result.success(itemService.list(page, size, sort));
    }

    @GetMapping("/category/{categoryId}")
    public Result<PageResult<ItemResponse>> listByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "time_desc") String sort) {
        return Result.success(itemService.listByCategory(categoryId, page, size, sort));
    }

    @GetMapping("/user/{userId}")
    public Result<PageResult<ItemResponse>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(itemService.listByUser(userId, page, size));
    }

    @GetMapping("/search")
    public Result<PageResult<ItemResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "time_desc") String sort) {
        return Result.success(itemService.search(keyword, page, size, sort));
    }

    @PutMapping("/{id}")
    public Result<ItemResponse> update(@PathVariable Long id, @Valid @RequestBody ItemCreateRequest request) {
        return Result.success(itemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        itemService.updateStatus(id, status);
        return Result.success();
    }
}