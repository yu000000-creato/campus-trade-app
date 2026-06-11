package com.example.campustrade.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal currentPrice;
    private String images;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;
}