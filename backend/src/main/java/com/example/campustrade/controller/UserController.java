package com.example.campustrade.controller;

import com.example.campustrade.dto.Result;
import com.example.campustrade.dto.request.UserLoginRequest;
import com.example.campustrade.dto.request.UserRegisterRequest;
import com.example.campustrade.dto.response.UserResponse;
import com.example.campustrade.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.register(request);
        return Result.success("注册成功", response);
    }

    @PostMapping("/login")
    public Result<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        UserResponse response = userService.getById(id);
        return Result.success(response);
    }

    @PutMapping("/{id}")
    public Result<UserResponse> update(@PathVariable Long id, @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.update(id, request);
        return Result.success("更新成功", response);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}