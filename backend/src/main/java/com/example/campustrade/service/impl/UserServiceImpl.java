package com.example.campustrade.service.impl;

import com.example.campustrade.dto.request.UserLoginRequest;
import com.example.campustrade.dto.request.UserRegisterRequest;
import com.example.campustrade.dto.response.UserResponse;
import com.example.campustrade.entity.User;
import com.example.campustrade.exception.BusinessException;
import com.example.campustrade.repository.UserRepository;
import com.example.campustrade.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(400, "手机号已被注册");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(400, "邮箱已被注册");
        }
        if (request.getStudentId() != null && userRepository.existsByStudentId(request.getStudentId())) {
            throw new BusinessException(400, "学号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(md5(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setStudentId(request.getStudentId());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Override
    public UserResponse login(UserLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));

        String encryptedPassword = md5(request.getPassword());
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(400, "账号已被禁用");
        }

        return toResponse(user);
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException(400, "用户名已存在");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }

        user.setRealName(request.getRealName());
        user.setStudentId(request.getStudentId());
        
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException(400, "手机号已被使用");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(400, "邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(404, "用户不存在");
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setStudentId(user.getStudentId());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}