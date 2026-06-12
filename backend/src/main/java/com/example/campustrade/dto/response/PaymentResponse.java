package com.example.campustrade.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Boolean success;
    
    private String message;
    
    private Long orderId;
    
    private String transactionId;
    
    private String paymentMethod;
}