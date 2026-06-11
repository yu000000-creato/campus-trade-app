package com.example.campustrade.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateRequest {

    private Long categoryId;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description;

    private BigDecimal originalPrice;

    @NotNull(message = "商品价格不能为空")
    private BigDecimal currentPrice;

    private String images;
}