package com.example.campustrade.controller;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.Result;
import com.example.campustrade.dto.request.OrderCreateRequest;
import com.example.campustrade.dto.response.OrderResponse;
import com.example.campustrade.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderResponse> create(@RequestParam Long buyerId, @Valid @RequestBody OrderCreateRequest request) {
        return Result.success("下单成功", orderService.create(buyerId, request));
    }

    @GetMapping("/{id}")
    public Result<OrderResponse> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/buyer/{buyerId}")
    public Result<PageResult<OrderResponse>> listByBuyer(
            @PathVariable Long buyerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.listByBuyer(buyerId, page, size, status));
    }

    @GetMapping("/seller/{sellerId}")
    public Result<PageResult<OrderResponse>> listBySeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.listBySeller(sellerId, page, size, status));
    }

    @PutMapping("/{id}/status")
    public Result<OrderResponse> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(orderService.updateStatus(id, status));
    }
    
    @DeleteMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success();
    }
}