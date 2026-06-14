package com.example.campustrade.service;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.request.OrderCreateRequest;
import com.example.campustrade.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse create(Long buyerId, OrderCreateRequest request);

    OrderResponse getById(Long id);

    OrderResponse getByOrderNo(String orderNo);

    PageResult<OrderResponse> listByBuyer(Long buyerId, Integer page, Integer size, Integer status);

    PageResult<OrderResponse> listBySeller(Long sellerId, Integer page, Integer size, Integer status);

    OrderResponse updateStatus(Long id, Integer status);

    void delete(Long id);
    
    void cancelOrder(Long id);
}