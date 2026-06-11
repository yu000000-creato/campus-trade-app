package com.example.campustrade.service;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.request.ChatSendRequest;
import com.example.campustrade.dto.response.ChatResponse;

import java.util.List;

public interface ChatService {

    ChatResponse send(Long senderId, ChatSendRequest request);

    ChatResponse sendMessage(Long senderId, Long receiverId, String content);

    PageResult<ChatResponse> list(Long userId1, Long userId2, Integer page, Integer size);

    List<ChatResponse> getChats(Long userId);

    List<ChatResponse> getConversation(Long userId, Long otherUserId);

    void markRead(Long receiverId);
}