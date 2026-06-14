package com.example.campustrade.service;

import com.example.campustrade.dto.request.UserLoginRequest;
import com.example.campustrade.dto.request.UserRegisterRequest;
import com.example.campustrade.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse register(UserRegisterRequest request);

    UserResponse login(UserLoginRequest request);

    UserResponse getById(Long id);

    UserResponse update(Long id, UserRegisterRequest request);

    void delete(Long id);

    UserResponse uploadAvatar(Long id, MultipartFile file);
}