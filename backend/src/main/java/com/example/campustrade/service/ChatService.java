package com.example.campustrade.service;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.request.ChatSendRequest;
import com.example.campustrade.dto.response.ChatResponse;

public interface ChatService {

    ChatResponse send(Long senderId, ChatSendRequest request);

    PageResult<ChatResponse> list(Long userId1, Long userId2, Integer page, Integer size);

    void markRead(Long receiverId);
}