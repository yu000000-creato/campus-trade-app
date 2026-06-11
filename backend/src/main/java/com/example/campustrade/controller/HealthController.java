package com.example.campustrade.controller;

import com.example.campustrade.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "campus-trade");
        return Result.success(data);
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("测试成功！服务器运行正常");
    }
}