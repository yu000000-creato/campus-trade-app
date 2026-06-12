package com.example.campustrade.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    
    private Long orderId;
    
    private String paymentMethod;
    
    private Double amount;
}