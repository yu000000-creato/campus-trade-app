package com.example.campustrade.service.impl;

import com.example.campustrade.dto.request.PaymentRequest;
import com.example.campustrade.dto.response.PaymentResponse;
import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.Order;
import com.example.campustrade.repository.ItemRepository;
import com.example.campustrade.repository.OrderRepository;
import com.example.campustrade.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final Random random = new Random();
    
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("创建支付订单 - 订单ID: {}, 支付方式: {}, 金额: {}", 
                request.getOrderId(), request.getPaymentMethod(), request.getAmount());
        
        // 验证订单
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 验证金额
        if (Double.compare(order.getPrice().doubleValue(), request.getAmount()) != 0) {
            throw new RuntimeException("支付金额与订单金额不一致");
        }
        
        // 调用模拟支付
        return simulatePayment(request);
    }
    
    @Override
    public PaymentResponse simulatePayment(PaymentRequest request) {
        log.info("模拟支付 - 订单ID: {}, 支付方式: {}", request.getOrderId(), request.getPaymentMethod());
        
        // 模拟支付成功率（80%成功）
        boolean success = random.nextDouble() > 0.2;
        
        // 生成交易ID
        String transactionId = generateTransactionId(request.getPaymentMethod());
        
        if (success) {
            // 更新订单状态为已支付 (2表示已支付)
            updateOrderStatus(request.getOrderId(), 2);
            
            // 支付成功后，将商品状态改为已售出
            updateItemStatus(request.getOrderId(), 2);
            log.info("支付成功 - 订单ID: {}, 交易ID: {}", request.getOrderId(), transactionId);
            return PaymentResponse.builder()
                    .success(true)
                    .message("支付成功")
                    .orderId(request.getOrderId())
                    .transactionId(transactionId)
                    .paymentMethod(request.getPaymentMethod())
                    .build();
        } else {
            log.warn("支付失败 - 订单ID: {}", request.getOrderId());
            return PaymentResponse.builder()
                    .success(false)
                    .message("支付失败，请重试")
                    .orderId(request.getOrderId())
                    .transactionId(transactionId)
                    .paymentMethod(request.getPaymentMethod())
                    .build();
        }
    }
    
    private String generateTransactionId(String paymentMethod) {
        String prefix = switch (paymentMethod.toLowerCase()) {
            case "wechat" -> "WX";
            case "alipay" -> "ALIPAY";
            default -> "TEST";
        };
        return prefix + "_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
    }
    
    private void updateOrderStatus(Long orderId, Integer status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }
    
    private void updateItemStatus(Long orderId, Integer status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            itemRepository.findById(order.getItemId()).ifPresent(item -> {
                item.setStatus(status);
                itemRepository.save(item);
            });
        });
    }
}