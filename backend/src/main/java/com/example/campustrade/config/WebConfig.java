package com.example.campustrade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加文件系统路径支持
        String imagesPath = "file:" + System.getProperty("user.dir") + "/backend/src/main/resources/images/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/", imagesPath);
        
        // 添加上传目录支持
        String uploadsPath = "file:" + System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsPath);
    }
}