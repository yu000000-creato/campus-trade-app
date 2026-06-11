package com.example.campustrade.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String realName;
    private String studentId;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private LocalDateTime createdAt;
}