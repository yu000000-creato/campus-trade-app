package com.example.campustrade.service.impl;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.request.ItemCreateRequest;
import com.example.campustrade.dto.response.ItemResponse;
import com.example.campustrade.entity.Category;
import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.exception.BusinessException;
import com.example.campustrade.repository.CategoryRepository;
import com.example.campustrade.repository.ItemRepository;
import com.example.campustrade.repository.UserRepository;
import com.example.campustrade.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ItemResponse create(Long userId, ItemCreateRequest request) {
        // 打印调试信息
        System.out.println("=== 创建商品 ===");
        System.out.println("userId: " + userId);
        System.out.println("request: " + request);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    System.out.println("错误：用户不存在，userId=" + userId);
                    return new BusinessException(404, "用户不存在");
                });
        
        System.out.println("找到用户: " + user.getUsername());

        Item item = new Item();
        item.setUserId(userId);
        item.setUsername(user.getUsername()); // 添加用户名
        item.setCategoryId(request.getCategoryId());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setOriginalPrice(request.getOriginalPrice());
        item.setCurrentPrice(request.getCurrentPrice());
        item.setImages(request.getImages());
        item.setStatus(1);
        item.setViewCount(0);

        System.out.println("准备保存商品: " + item.getTitle());
        Item savedItem = itemRepository.save(item);
        System.out.println("商品保存成功，id=" + savedItem.getId());
        
        return toResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        
        itemRepository.incrementViewCount(id);
        return toResponse(item);
    }

    @Override
    public PageResult<ItemResponse> list(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemPage = itemRepository.findByStatus(1, pageable);
        return PageResult.of(
                itemPage.getContent().stream().map(this::toResponse).toList(),
                itemPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageResult<ItemResponse> listByCategory(Long categoryId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemPage = itemRepository.findByCategoryIdAndStatus(categoryId, 1, pageable);
        return PageResult.of(
                itemPage.getContent().stream().map(this::toResponse).toList(),
                itemPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageResult<ItemResponse> listByUser(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemPage = itemRepository.findByUserId(userId, pageable);
        return PageResult.of(
                itemPage.getContent().stream().map(this::toResponse).toList(),
                itemPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageResult<ItemResponse> search(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemPage = itemRepository.searchByKeyword(keyword, 1, pageable);
        return PageResult.of(
                itemPage.getContent().stream().map(this::toResponse).toList(),
                itemPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    @Transactional
    public ItemResponse update(Long id, ItemCreateRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));

        if (request.getCategoryId() != null) {
            item.setCategoryId(request.getCategoryId());
        }
        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getOriginalPrice() != null) {
            item.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getCurrentPrice() != null) {
            item.setCurrentPrice(request.getCurrentPrice());
        }
        if (request.getImages() != null) {
            item.setImages(request.getImages());
        }

        Item updatedItem = itemRepository.save(item);
        return toResponse(updatedItem);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new BusinessException(404, "商品不存在");
        }
        itemRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        item.setStatus(status);
        itemRepository.save(item);
    }

    private ItemResponse toResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setUserId(item.getUserId());
        
        User user = userRepository.findById(item.getUserId()).orElse(null);
        response.setUsername(user != null ? user.getUsername() : "");
        
        response.setCategoryId(item.getCategoryId());
        Category category = categoryRepository.findById(item.getCategoryId()).orElse(null);
        response.setCategoryName(category != null ? category.getName() : "");
        
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());
        response.setOriginalPrice(item.getOriginalPrice());
        response.setCurrentPrice(item.getCurrentPrice());
        response.setImages(item.getImages());
        response.setStatus(item.getStatus());
        response.setViewCount(item.getViewCount());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }
}