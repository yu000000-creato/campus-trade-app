package com.example.campustrade.controller;

import com.example.campustrade.dto.request.PaymentRequest;
import com.example.campustrade.dto.response.PaymentResponse;
import com.example.campustrade.dto.Result;
import com.example.campustrade.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/create")
    public ResponseEntity<Result<PaymentResponse>> createPayment(@RequestBody PaymentRequest request) {
        log.info("收到支付请求 - 订单ID: {}, 支付方式: {}, 金额: {}", 
                request.getOrderId(), request.getPaymentMethod(), request.getAmount());
        
        try {
            PaymentResponse response = paymentService.createPayment(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(Result.success(response));
            } else {
                return ResponseEntity.ok(Result.error(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("支付处理失败", e);
            return ResponseEntity.ok(Result.error("支付失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/simulate")
    public ResponseEntity<Result<PaymentResponse>> simulatePayment(@RequestBody PaymentRequest request) {
        log.info("收到模拟支付请求 - 订单ID: {}, 支付方式: {}", 
                request.getOrderId(), request.getPaymentMethod());
        
        try {
            PaymentResponse response = paymentService.simulatePayment(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(Result.success(response));
            } else {
                return ResponseEntity.ok(Result.error(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("模拟支付处理失败", e);
            return ResponseEntity.ok(Result.error("支付失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/methods")
    public ResponseEntity<Result<String[]>> getPaymentMethods() {
        String[] methods = {"wechat", "alipay", "balance"};
        return ResponseEntity.ok(Result.success(methods));
    }
}