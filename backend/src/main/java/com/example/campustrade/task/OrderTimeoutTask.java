package com.example.campustrade.task;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.Order;
import com.example.campustrade.repository.ItemRepository;
import com.example.campustrade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    /**
     * 每分钟检查一次超时订单
     * 超过30分钟未支付的订单自动取消
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    @Transactional
    public void cancelTimeoutOrders() {
        log.info("开始检查超时订单...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 查找所有待付款且已超时的订单
        List<Order> timeoutOrders = orderRepository.findTimeoutOrders(now);
        
        if (timeoutOrders.isEmpty()) {
            log.info("没有超时订单");
            return;
        }
        
        log.info("发现 {} 个超时订单", timeoutOrders.size());
        
        for (Order order : timeoutOrders) {
            try {
                // 取消订单
                order.setStatus(5); // 5表示已取消
                orderRepository.save(order);
                
                // 恢复商品状态为可售
                Item item = itemRepository.findById(order.getItemId()).orElse(null);
                if (item != null) {
                    item.setStatus(1); // 恢复为可售状态
                    itemRepository.save(item);
                    log.info("订单 {} 已取消，商品 {} 已恢复为可售状态", 
                            order.getOrderNo(), item.getTitle());
                }
                
            } catch (Exception e) {
                log.error("取消订单 {} 失败: {}", order.getOrderNo(), e.getMessage());
            }
        }
        
        log.info("超时订单检查完成");
    }
}