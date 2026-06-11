package com.example.campustrade.repository;

import com.example.campustrade.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    @Query("SELECT c FROM Chat c WHERE (c.senderId = :userId1 AND c.receiverId = :userId2) " +
           "OR (c.senderId = :userId2 AND c.receiverId = :userId1) ORDER BY c.createdAt ASC")
    Page<Chat> findByTwoUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);
    
    @Query("SELECT c FROM Chat c WHERE (c.senderId = :userId1 AND c.receiverId = :userId2) " +
           "OR (c.senderId = :userId2 AND c.receiverId = :userId1) ORDER BY c.createdAt ASC")
    List<Chat> findByTwoUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    @Query("SELECT c FROM Chat c WHERE c.senderId = :userId OR c.receiverId = :userId ORDER BY c.createdAt DESC")
    List<Chat> findByUserId(@Param("userId") Long userId);
    
    List<Chat> findByReceiverIdAndStatus(Long receiverId, Integer status);
    
    @Modifying
    @Query("UPDATE Chat c SET c.status = :status WHERE c.receiverId = :receiverId AND c.status = :oldStatus")
    int updateStatusByReceiverId(@Param("receiverId") Long receiverId, @Param("oldStatus") Integer oldStatus, @Param("status") Integer status);
}