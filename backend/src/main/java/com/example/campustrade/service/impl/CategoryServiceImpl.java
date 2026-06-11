package com.example.campustrade.service.impl;

import com.example.campustrade.entity.Category;
import com.example.campustrade.exception.BusinessException;
import com.example.campustrade.repository.CategoryRepository;
import com.example.campustrade.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> list() {
        return categoryRepository.findByStatusOrderBySortOrderAsc(1);
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
    }

    @Override
    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new BusinessException(400, "分类名称已存在");
        }
        category.setStatus(1);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(Long id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));

        if (!existing.getName().equals(category.getName()) && categoryRepository.existsByName(category.getName())) {
            throw new BusinessException(400, "分类名称已存在");
        }

        existing.setName(category.getName());
        existing.setIcon(category.getIcon());
        existing.setSortOrder(category.getSortOrder());
        existing.setStatus(category.getStatus());

        return categoryRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(404, "分类不存在");
        }
        categoryRepository.deleteById(id);
    }
}