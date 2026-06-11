package com.example.campustrade.service;

import com.example.campustrade.dto.response.UserResponse;
import com.example.campustrade.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> list();

    Category getById(Long id);

    Category create(Category category);

    Category update(Long id, Category category);

    void delete(Long id);
}