package com.example.campustrade.repository;

import com.example.campustrade.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByStatus(Integer status);
    
    List<Category> findByStatusOrderBySortOrderAsc(Integer status);
    
    boolean existsByName(String name);
}