package com.example.campustrade.repository;

import com.example.campustrade.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    Page<Item> findByStatus(Integer status, Pageable pageable);
    
    Page<Item> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
    
    Page<Item> findByUserId(Long userId, Pageable pageable);
    
    Page<Item> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
    
    List<Item> findByStatusOrderByCreatedAtDesc(Integer status);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status AND " +
           "(i.title LIKE %:keyword% OR i.description LIKE %:keyword%)")
    Page<Item> searchByKeyword(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);
    
    @Modifying
    @Query("UPDATE Item i SET i.viewCount = i.viewCount + 1 WHERE i.id = :id")
    int incrementViewCount(@Param("id") Long id);
}