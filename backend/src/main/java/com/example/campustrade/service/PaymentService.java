package com.example.campustrade.service;

import com.example.campustrade.dto.request.PaymentRequest;
import com.example.campustrade.dto.response.PaymentResponse;

public interface PaymentService {
    
    PaymentResponse createPayment(PaymentRequest request);
    
    PaymentResponse simulatePayment(PaymentRequest request);
}