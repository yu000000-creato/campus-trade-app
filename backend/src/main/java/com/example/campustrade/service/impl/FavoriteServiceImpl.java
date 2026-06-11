package com.example.campustrade.service.impl;

import com.example.campustrade.dto.PageResult;
import com.example.campustrade.dto.response.ItemResponse;
import com.example.campustrade.entity.Favorite;
import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.exception.BusinessException;
import com.example.campustrade.repository.CategoryRepository;
import com.example.campustrade.repository.FavoriteRepository;
import com.example.campustrade.repository.ItemRepository;
import com.example.campustrade.repository.UserRepository;
import com.example.campustrade.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void add(Long userId, Long itemId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!itemRepository.existsById(itemId)) {
            throw new BusinessException(404, "商品不存在");
        }
        if (favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new BusinessException(400, "已收藏该商品");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long itemId) {
        if (!favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new BusinessException(400, "未收藏该商品");
        }
        favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    @Override
    public PageResult<ItemResponse> list(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favoritePage = favoriteRepository.findByUserId(userId, pageable);
        
        return PageResult.of(
                favoritePage.getContent().stream()
                        .map(f -> toItemResponse(itemRepository.findById(f.getItemId()).orElse(null)))
                        .filter(r -> r != null)
                        .toList(),
                favoritePage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public boolean isFavorite(Long userId, Long itemId) {
        return favoriteRepository.existsByUserIdAndItemId(userId, itemId);
    }

    private ItemResponse toItemResponse(Item item) {
        if (item == null) return null;
        
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setUserId(item.getUserId());
        
        User user = userRepository.findById(item.getUserId()).orElse(null);
        response.setUsername(user != null ? user.getUsername() : "");
        
        response.setCategoryId(item.getCategoryId());
        com.example.campustrade.entity.Category category = categoryRepository.findById(item.getCategoryId()).orElse(null);
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