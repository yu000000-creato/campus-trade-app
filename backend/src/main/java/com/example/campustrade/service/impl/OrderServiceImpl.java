package com.example.campustrade.service.impl;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.request.OrderCreateRequest;
import com.example.campustrade.dto.response.OrderResponse;
import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.Order;
import com.example.campustrade.entity.User;
import com.example.campustrade.exception.BusinessException;
import com.example.campustrade.repository.ItemRepository;
import com.example.campustrade.repository.OrderRepository;
import com.example.campustrade.repository.UserRepository;
import com.example.campustrade.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse create(Long buyerId, OrderCreateRequest request) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(404, "买家不存在"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));

        if (item.getStatus() != 1) {
            throw new BusinessException(400, "商品已下架或已卖出");
        }

        if (item.getUserId().equals(buyerId)) {
            throw new BusinessException(400, "不能购买自己的商品");
        }

        // 检查是否已经有未支付的订单
        if (orderRepository.existsByItemIdAndBuyerIdAndStatus(request.getItemId(), buyerId, 1)) {
            throw new BusinessException(400, "您已经有一个待付款的订单，请先完成支付或取消订单");
        }

        User seller = userRepository.findById(item.getUserId())
                .orElseThrow(() -> new BusinessException(404, "卖家不存在"));

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setItemId(request.getItemId());
        order.setBuyerId(buyerId);
        order.setSellerId(item.getUserId());
        order.setPrice(item.getCurrentPrice());
        order.setStatus(1);
        order.setAddress(request.getAddress());
        order.setPhone(request.getPhone());
        order.setRemark(request.getRemark());
        // 设置支付截止时间为30分钟后（使用UTC时间）
        order.setPaymentDeadline(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30));

        Order savedOrder = orderRepository.save(order);
        
        // 不立即修改商品状态，支付成功后再修改

        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        return toResponse(order);
    }

    @Override
    public OrderResponse getByOrderNo(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        return toResponse(order);
    }

    @Override
    public PageResult<OrderResponse> listByBuyer(Long buyerId, Integer page, Integer size, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage;
        
        if (status != null && status > 0) {
            orderPage = orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        } else {
            orderPage = orderRepository.findByBuyerId(buyerId, pageable);
        }
        
        return PageResult.of(
                orderPage.getContent().stream().map(this::toResponse).toList(),
                orderPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageResult<OrderResponse> listBySeller(Long sellerId, Integer page, Integer size, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage;
        
        if (status != null && status > 0) {
            orderPage = orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        } else {
            orderPage = orderRepository.findBySellerId(sellerId, pageable);
        }
        
        return PageResult.of(
                orderPage.getContent().stream().map(this::toResponse).toList(),
                orderPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, Integer status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderInfo(Long id, String address, String phone) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        
        if (address != null) {
            order.setAddress(address);
        }
        if (phone != null) {
            order.setPhone(phone);
        }
        Order updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new BusinessException(404, "订单不存在");
        }
        orderRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        
        if (order.getStatus() != 1) {
            throw new BusinessException(400, "只有待付款订单可以取消");
        }
        
        // 取消订单
        order.setStatus(5);
        orderRepository.save(order);
        
        // 恢复商品状态为可售
        Item item = itemRepository.findById(order.getItemId()).orElse(null);
        if (item != null) {
            item.setStatus(1);
            itemRepository.save(item);
        }
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + (int) (Math.random() * 1000);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setItemId(order.getItemId());
        
        Item item = itemRepository.findById(order.getItemId()).orElse(null);
        response.setItemTitle(item != null ? item.getTitle() : "");
        
        response.setBuyerId(order.getBuyerId());
        User buyer = userRepository.findById(order.getBuyerId()).orElse(null);
        response.setBuyerName(buyer != null ? buyer.getUsername() : "");
        
        response.setSellerId(order.getSellerId());
        User seller = userRepository.findById(order.getSellerId()).orElse(null);
        response.setSellerName(seller != null ? seller.getUsername() : "");
        
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        response.setStatusText(getStatusText(order.getStatus()));
        response.setAddress(order.getAddress());
        response.setPhone(order.getPhone());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        response.setPaymentDeadline(order.getPaymentDeadline() != null ? order.getPaymentDeadline().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return response;
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 1 -> "待付款";
            case 2 -> "待发货";
            case 3 -> "待收货";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}