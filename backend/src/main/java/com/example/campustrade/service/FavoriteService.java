package com.example.campustrade.service;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.response.ItemResponse;

public interface FavoriteService {

    void add(Long userId, Long itemId);

    void remove(Long userId, Long itemId);

    PageResult<ItemResponse> list(Long userId, Integer page, Integer size);

    boolean isFavorite(Long userId, Long itemId);
}