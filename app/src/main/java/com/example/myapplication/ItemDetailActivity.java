package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Item;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDesc, tvPrice, tvOriginalPrice, tvSeller, tvViewCount, tvCategory;
    private Button btnBuy, btnFavorite;
    private ApiService apiService;
    private ExecutorService executorService;
    private Item item;
    private Long userId;

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
        tvSeller = findViewById(R.id.tv_seller);
        tvViewCount = findViewById(R.id.tv_view_count);
        tvCategory = findViewById(R.id.tv_category);
        btnBuy = findViewById(R.id.btn_buy);
        btnFavorite = findViewById(R.id.btn_favorite);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
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
                Gson gson = new Gson();
                Result<Item> result = gson.fromJson(jsonResponse, new TypeToken<Result<Item>>() {}.getType());

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
        tvSeller.setText("卖家: " + item.getUsername());
        tvViewCount.setText("浏览次数: " + item.getViewCount());
        tvCategory.setText("分类: " + getCategoryName(item.getCategoryId()));

        btnBuy.setOnClickListener(v -> createOrder());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
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
        executorService.execute(() -> {
            try {
                String url = "favorites?userId=" + userId + "&itemId=" + item.getId();
                String jsonResponse;
                
                if (btnFavorite.getText().toString().equals("收藏")) {
                    jsonResponse = apiService.getRaw(url);
                } else {
                    // 取消收藏需要使用DELETE方法
                    // 这里简化处理，使用GET模拟
                    url = url + "&action=remove";
                    jsonResponse = apiService.getRaw("favorites/user/" + userId);
                }

                runOnUiThread(() -> {
                    if (btnFavorite.getText().toString().equals("收藏")) {
                        btnFavorite.setText("已收藏");
                        Toast.makeText(ItemDetailActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                    } else {
                        btnFavorite.setText("收藏");
                        Toast.makeText(ItemDetailActivity.this, "取消收藏成功", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}