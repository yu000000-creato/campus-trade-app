package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Item;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDesc, tvPrice, tvOriginalPrice, tvSeller, tvViewCount, tvCategory;
    private ImageView ivItemImage;
    private TextView tvImageHint;
    private Button btnBuy, btnFavorite, btnChat;
    private ApiService apiService;
    private ExecutorService executorService;
    private Item item;
    private Long userId;
    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        initViews();
        loadUserInfo();
        loadItemDetail();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvDesc = findViewById(R.id.tv_desc);
        tvPrice = findViewById(R.id.tv_price);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvViewCount = findViewById(R.id.tv_view_count);
        tvCategory = findViewById(R.id.tv_category);
        ivItemImage = findViewById(R.id.iv_item_image);
        tvImageHint = findViewById(R.id.tv_image_hint);
        btnBuy = findViewById(R.id.btn_buy);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnChat = findViewById(R.id.btn_chat);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        imageUrls = new ArrayList<>();

        // 图片点击预览
        ivItemImage.setOnClickListener(v -> {
            if (imageUrls != null && !imageUrls.isEmpty()) {
                Intent intent = new Intent(ItemDetailActivity.this, ImagePreviewActivity.class);
                intent.putStringArrayListExtra(ImagePreviewActivity.EXTRA_IMAGE_URLS, new ArrayList<>(imageUrls));
                intent.putExtra(ImagePreviewActivity.EXTRA_CURRENT_INDEX, 0);
                startActivity(intent);
            }
        });
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);
    }

    private void loadItemDetail() {
        Long itemId = getIntent().getLongExtra("item_id", 0);
        
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("items/" + itemId);
                // 使用 ApiService 中配置好的 Gson 实例
                Result<Item> result = apiService.getGson().fromJson(jsonResponse, new TypeToken<Result<Item>>() {}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    item = result.getData();
                    runOnUiThread(() -> displayItemDetail(item));
                    checkFavoriteStatus();
                }
            } catch (Exception e) {
                Log.e("ItemDetailActivity", "Load item error: " + e.getMessage());
            }
        });
    }

    private void displayItemDetail(Item item) {
        tvTitle.setText(item.getTitle());
        tvDesc.setText(item.getDescription() != null ? item.getDescription() : "暂无描述");
        tvPrice.setText("¥" + item.getCurrentPrice());
        tvOriginalPrice.setText("原价: ¥" + (item.getOriginalPrice() != null ? item.getOriginalPrice() : item.getCurrentPrice()));
        tvViewCount.setText("浏览次数: " + item.getViewCount());
        tvCategory.setText("分类: " + getCategoryName(item.getCategoryId()));

        // 显示商品图片
        loadItemImage(item.getImages());

        btnBuy.setOnClickListener(v -> createOrder());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnChat.setOnClickListener(v -> chatWithSeller());
    }

    private void loadItemImage(String imagesJson) {
        imageUrls.clear();
        if (imagesJson != null && !imagesJson.isEmpty()) {
            try {
                List<String> images = new Gson().fromJson(imagesJson, new TypeToken<List<String>>() {}.getType());
                if (images != null && !images.isEmpty()) {
                    imageUrls.addAll(images);
                    String imageUrl = images.get(0);
                    // 处理相对路径
                    if (imageUrl.startsWith("/")) {
                        imageUrl = "http://10.0.2.2:8080" + imageUrl;
                    }
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .centerCrop()
                            .into(ivItemImage);
                    tvImageHint.setVisibility(View.VISIBLE);
                    return;
                }
            } catch (Exception e) {
                Log.e("ItemDetailActivity", "Parse images error: " + e.getMessage());
            }
        }
        // 没有图片时显示默认图
        ivItemImage.setImageResource(R.drawable.ic_launcher_background);
        tvImageHint.setVisibility(View.GONE);
    }

    private String getCategoryName(Long categoryId) {
        String[] categories = {"", "数码产品", "图书教材", "生活用品", "运动器材", "服装配饰", "其他物品"};
        if (categoryId != null && categoryId > 0 && categoryId < categories.length) {
            return categories[categoryId.intValue()];
        }
        return "未知分类";
    }

    private void checkFavoriteStatus() {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("favorites/check?userId=" + userId + "&itemId=" + item.getId());
                Gson gson = new Gson();
                Result<Boolean> result = gson.fromJson(jsonResponse, new TypeToken<Result<Boolean>>() {}.getType());

                if (result != null && result.isSuccess() && Boolean.TRUE.equals(result.getData())) {
                    runOnUiThread(() -> btnFavorite.setText("已收藏"));
                } else {
                    runOnUiThread(() -> btnFavorite.setText("收藏"));
                }
            } catch (Exception e) {
                Log.e("ItemDetailActivity", "Check favorite error: " + e.getMessage());
            }
        });
    }

    private void toggleFavorite() {
        // 在操作前记录当前状态，避免异步操作中按钮文字被修改
        boolean isFavorite = btnFavorite.getText().toString().equals("已收藏");
        
        executorService.execute(() -> {
            try {
                String url = "favorites?userId=" + userId + "&itemId=" + item.getId();
                Result<String> result;
                
                if (!isFavorite) {
                    // 当前未收藏，执行添加收藏
                    result = apiService.postForm(url, "", String.class);
                } else {
                    // 当前已收藏，执行取消收藏
                    result = apiService.delete("favorites?userId=" + userId + "&itemId=" + item.getId(), String.class);
                }

                runOnUiThread(() -> {
                    if (result != null && result.isSuccess()) {
                        if (!isFavorite) {
                            btnFavorite.setText("已收藏");
                            Toast.makeText(ItemDetailActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                        } else {
                            btnFavorite.setText("收藏");
                            Toast.makeText(ItemDetailActivity.this, "取消收藏成功", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = result != null ? result.getMessage() : "操作失败";
                        Toast.makeText(ItemDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("ItemDetailActivity", "Favorite error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ItemDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void createOrder() {
        executorService.execute(() -> {
            String jsonBody = "{\"itemId\": " + item.getId() + ", \"address\": \"默认地址\", \"remark\": \"\"}";
            Result<?> result = apiService.post("orders?buyerId=" + userId, jsonBody, Object.class);

            runOnUiThread(() -> {
                if (result != null && result.isSuccess()) {
                    Toast.makeText(ItemDetailActivity.this, "下单成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ItemDetailActivity.this, OrderActivity.class);
                    startActivity(intent);
                } else {
                    String errorMsg = result != null && result.getMessage() != null ? result.getMessage() : "下单失败";
                    Toast.makeText(ItemDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void chatWithSeller() {
        Log.d("ItemDetailActivity", "chatWithSeller called");
        Log.d("ItemDetailActivity", "userId: " + userId);
        Log.d("ItemDetailActivity", "item: " + item);
        
        // 检查用户是否登录
        if (userId == 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            Log.e("ItemDetailActivity", "userId is 0, not logged in");
            // 跳转到登录页面
            Intent intent = new Intent(ItemDetailActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        
        if (item == null) {
            Toast.makeText(this, "商品信息加载失败", Toast.LENGTH_SHORT).show();
            Log.e("ItemDetailActivity", "item is null");
            return;
        }
        
        if (item.getUserId() == null) {
            Toast.makeText(this, "无法获取卖家ID", Toast.LENGTH_SHORT).show();
            Log.e("ItemDetailActivity", "item.getUserId() is null");
            return;
        }

        Log.d("ItemDetailActivity", "sellerId: " + item.getUserId());
        Log.d("ItemDetailActivity", "sellerName: " + item.getUsername());

        if (item.getUserId().equals(userId)) {
            Toast.makeText(ItemDetailActivity.this, "不能与自己聊天", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ItemDetailActivity.this, ChatActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("sellerId", item.getUserId());
        intent.putExtra("sellerName", item.getUsername());
        intent.putExtra("itemId", item.getId());
        intent.putExtra("itemTitle", item.getTitle());
        Log.d("ItemDetailActivity", "Starting ChatActivity with sellerId: " + item.getUserId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}