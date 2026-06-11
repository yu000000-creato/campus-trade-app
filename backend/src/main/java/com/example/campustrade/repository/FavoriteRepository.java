package com.example.campustrade.repository;

import com.example.campustrade.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    Page<Favorite> findByUserId(Long userId, Pageable pageable);
    
    Optional<Favorite> findByUserIdAndItemId(Long userId, Long itemId);
    
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
    
    void deleteByUserIdAndItemId(Long userId, Long itemId);
}