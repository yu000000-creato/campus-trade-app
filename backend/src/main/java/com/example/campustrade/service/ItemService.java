package com.example.campustrade.service;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.request.ItemCreateRequest;
import com.example.campustrade.dto.response.ItemResponse;

public interface ItemService {

    ItemResponse create(Long userId, ItemCreateRequest request);

    ItemResponse getById(Long id);

    PageResult<ItemResponse> list(Integer page, Integer size);

    PageResult<ItemResponse> listByCategory(Long categoryId, Integer page, Integer size);

    PageResult<ItemResponse> listByUser(Long userId, Integer page, Integer size);

    PageResult<ItemResponse> search(String keyword, Integer page, Integer size);

    ItemResponse update(Long id, ItemCreateRequest request);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}