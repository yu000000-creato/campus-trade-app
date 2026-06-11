package com.example.campustrade.service.impl;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.request.ChatSendRequest;
import com.example.campustrade.dto.response.ChatResponse;
import com.example.campustrade.entity.Chat;
import com.example.campustrade.entity.User;
import com.example.campustrade.exception.BusinessException;
import com.example.campustrade.repository.ChatRepository;
import com.example.campustrade.repository.UserRepository;
import com.example.campustrade.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatResponse send(Long senderId, ChatSendRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(404, "发送者不存在"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new BusinessException(404, "接收者不存在"));

        Chat chat = new Chat();
        chat.setSenderId(senderId);
        chat.setReceiverId(request.getReceiverId());
        chat.setContent(request.getContent());
        chat.setStatus(0);

        Chat savedChat = chatRepository.save(chat);
        return toResponse(savedChat);
    }

    @Override
    public PageResult<ChatResponse> list(Long userId1, Long userId2, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Chat> chatPage = chatRepository.findByTwoUsers(userId1, userId2, pageable);
        
        chatRepository.updateStatusByReceiverId(userId1, 0, 1);
        
        return PageResult.of(
                chatPage.getContent().stream().map(this::toResponse).toList(),
                chatPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    @Transactional
    public void markRead(Long receiverId) {
        chatRepository.updateStatusByReceiverId(receiverId, 0, 1);
    }

    @Override
    @Transactional
    public ChatResponse sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(404, "发送者不存在"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(404, "接收者不存在"));

        Chat chat = new Chat();
        chat.setSenderId(senderId);
        chat.setReceiverId(receiverId);
        chat.setContent(content);
        chat.setStatus(0);

        Chat savedChat = chatRepository.save(chat);
        return toResponse(savedChat);
    }

    @Override
    public List<ChatResponse> getChats(Long userId) {
        List<Chat> chats = chatRepository.findByUserId(userId);
        return chats.stream().map(this::toResponse).toList();
    }

    @Override
    public List<ChatResponse> getConversation(Long userId, Long otherUserId) {
        List<Chat> chats = chatRepository.findByTwoUsers(userId, otherUserId);
        return chats.stream().map(this::toResponse).toList();
    }

    private ChatResponse toResponse(Chat chat) {
        ChatResponse response = new ChatResponse();
        response.setId(chat.getId());
        response.setSenderId(chat.getSenderId());
        
        User sender = userRepository.findById(chat.getSenderId()).orElse(null);
        response.setSenderName(sender != null ? sender.getUsername() : "");
        
        response.setReceiverId(chat.getReceiverId());
        User receiver = userRepository.findById(chat.getReceiverId()).orElse(null);
        response.setReceiverName(receiver != null ? receiver.getUsername() : "");
        
        response.setContent(chat.getContent());
        response.setStatus(chat.getStatus());
        response.setCreatedAt(chat.getCreatedAt() != null ? chat.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return response;
    }
}