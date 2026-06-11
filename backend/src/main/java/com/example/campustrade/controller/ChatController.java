package com.example.campustrade.controller;

import com.example.campustrade.dto.Result;
import com.example.campustrade.dto.request.ChatSendRequest;
import com.example.campustrade.dto.response.ChatResponse;
import com.example.campustrade.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{userId}")
    public Result<List<ChatResponse>> getChats(@PathVariable Long userId) {
        return Result.success(chatService.getChats(userId));
    }

    @GetMapping("/conversation/{userId}/{otherUserId}")
    public Result<List<ChatResponse>> getConversation(@PathVariable Long userId, @PathVariable Long otherUserId) {
        return Result.success(chatService.getConversation(userId, otherUserId));
    }

    @PostMapping
    public Result<ChatResponse> sendMessage(@RequestBody ChatSendRequest request) {
        return Result.success(chatService.sendMessage(request.getSenderId(), request.getReceiverId(), request.getContent()));
    }
}