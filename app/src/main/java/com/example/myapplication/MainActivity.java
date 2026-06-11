package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Item;
import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsername;
    private Button btnLogout;
    private EditText etSearch;
    private Button btnSearch;
    private LinearLayout llCategories;
    private LinearLayout llItems;
    private ApiService apiService;
    private ExecutorService executorService;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        loadUserInfo();
        loadCategories();
        loadItems();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        btnLogout = findViewById(R.id.btn_logout);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        llCategories = findViewById(R.id.ll_categories);
        llItems = findViewById(R.id.ll_items);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchItems(keyword);
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchItems(keyword);
            }
            return true;
        });

        findViewById(R.id.tab_favorite).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.tab_publish).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PublishActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.tab_order).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrderActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.tab_profile).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "");
        tvUsername.setText("欢迎, " + currentUsername);
    }

    private void logout() {
        getSharedPreferences("campus_trade", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadCategories() {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("categories");
                Gson gson = new Gson();
                Result<List<Category>> result = gson.fromJson(jsonResponse,
                        new TypeToken<Result<List<Category>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Category> categories = result.getData();
                    runOnUiThread(() -> displayCategories(categories));
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Load categories error: " + e.getMessage());
            }
        });
    }

    private void displayCategories(List<Category> categories) {
        llCategories.removeAllViews();

        for (Category category : categories) {
            TextView tvCategory = new TextView(this);
            tvCategory.setText(category.getName());
            tvCategory.setTextSize(14);
            tvCategory.setTextColor(Color.BLACK);
            tvCategory.setBackgroundColor(Color.parseColor("#f0f2f5"));
            tvCategory.setPadding(16, 8, 16, 8);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 12, 0);
            tvCategory.setLayoutParams(params);

            tvCategory.setOnClickListener(v -> filterByCategory(category.getId()));

            llCategories.addView(tvCategory);
        }
    }

    private void loadItems() {
        executorService.execute(() -> {
            try {
                Log.d("MainActivity", "开始加载商品列表...");
                String jsonResponse = apiService.getRaw("items?page=1&size=10");
                Log.d("MainActivity", "商品列表响应: " + jsonResponse);
                // 使用 ApiService 中配置好的 Gson 实例
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null) {
                    if (result.isSuccess()) {
                        if (result.getData() != null && result.getData().getList() != null) {
                            List<Item> items = result.getData().getList();
                            Log.d("MainActivity", "成功加载商品数量: " + items.size());
                            runOnUiThread(() -> displayItems(items));
                        } else {
                            Log.d("MainActivity", "商品数据为空");
                            runOnUiThread(() -> displayItems(null));
                        }
                    } else {
                        Log.e("MainActivity", "API返回失败: " + result.getMessage());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "加载失败: " + result.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("MainActivity", "API响应为空");
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "网络响应异常", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Load items error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void searchItems(String keyword) {
        executorService.execute(() -> {
            try {
                String url = "items/search?keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") + "&page=1&size=10";
                String jsonResponse = apiService.getRaw(url);
                // 使用 ApiService 中配置好的 Gson 实例
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    runOnUiThread(() -> displayItems(items));
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Search items error: " + e.getMessage());
            }
        });
    }

    private void filterByCategory(Long categoryId) {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("items/category/" + categoryId + "?page=1&size=10");
                // 使用 ApiService 中配置好的 Gson 实例
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    runOnUiThread(() -> displayItems(items));
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Filter items error: " + e.getMessage());
            }
        });
    }

    private void displayItems(List<Item> items) {
        llItems.removeAllViews();

        if (items == null || items.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("暂无商品");
            tvEmpty.setTextSize(16);
            tvEmpty.setTextColor(Color.parseColor("#718096"));
            tvEmpty.setPadding(0, 40, 0, 40);
            tvEmpty.setGravity(Gravity.CENTER);
            llItems.addView(tvEmpty);
            return;
        }

        for (Item item : items) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setBackgroundColor(Color.WHITE);
            itemLayout.setPadding(12, 12, 12, 12);

            LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemLayoutParams.setMargins(0, 0, 0, 12);
            itemLayout.setLayoutParams(itemLayoutParams);

            // 使用 ImageView 显示商品图片
            ImageView ivImage = new ImageView(this);
            ivImage.setBackgroundColor(Color.TRANSPARENT);  // 设置透明背景
            ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 120);
            ivImage.setLayoutParams(imageParams);

            // 加载图片
            String imageUrl = getFirstImageUrl(item.getImages());
            Log.d("MainActivity", "商品ID: " + item.getId() + ", 商品名称: " + item.getTitle() + ", 图片URL: " + imageUrl);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .timeout(15000)
                        .error(R.drawable.ic_launcher_background)
                        .fallback(R.drawable.ic_launcher_background)
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.e("MainActivity", "Glide加载失败 - 商品: " + item.getTitle() + ", URL: " + imageUrl + ", 错误: " + (e != null ? e.getMessage() : "未知"));
                                // 尝试使用系统默认图片加载
                                loadImageWithHttpURLConnection(imageUrl, ivImage);
                                return true;  // 返回true表示已处理错误
                            }
                            
                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d("MainActivity", "Glide加载成功 - 商品: " + item.getTitle());
                                return false;
                            }
                        })
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_launcher_background);
            }

            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            infoLayout.setPadding(12, 0, 0, 0);

            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            );
            infoLayout.setLayoutParams(infoParams);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(item.getTitle());
            tvTitle.setTextSize(16);
            tvTitle.setTextColor(Color.BLACK);
            tvTitle.setMaxLines(1);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(item.getDescription() != null ? item.getDescription() : "暂无描述");
            tvDesc.setTextSize(12);
            tvDesc.setTextColor(Color.parseColor("#718096"));
            tvDesc.setMaxLines(2);

            TextView tvPrice = new TextView(this);
            tvPrice.setText("¥" + item.getCurrentPrice());
            tvPrice.setTextSize(18);
            tvPrice.setTextColor(Color.RED);

            TextView tvMeta = new TextView(this);
            tvMeta.setText("卖家: " + item.getUsername() + " | 浏览: " + item.getViewCount());
            tvMeta.setTextSize(12);
            tvMeta.setTextColor(Color.parseColor("#718096"));

            infoLayout.addView(tvTitle);
            infoLayout.addView(tvDesc);
            infoLayout.addView(tvPrice);
            infoLayout.addView(tvMeta);

            itemLayout.addView(ivImage);
            itemLayout.addView(infoLayout);

            itemLayout.setOnClickListener(v -> showItemDetail(item));

            llItems.addView(itemLayout);
        }
    }

    private String getFirstImageUrl(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return null;
        }
        try {
            List<String> images = new Gson().fromJson(imagesJson, new TypeToken<List<String>>() {}.getType());
            if (images != null && !images.isEmpty()) {
                String imageUrl = images.get(0);
                // 如果是相对路径，添加服务器基础URL
                if (imageUrl.startsWith("/")) {
                    return "http://10.0.2.2:8080" + imageUrl;
                }
                return imageUrl;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Parse images error: " + e.getMessage());
        }
        return null;
    }

    private void showItemDetail(Item item) {
        Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
        intent.putExtra("item_id", item.getId());
        startActivity(intent);
    }

    // 备用图片加载方法 - 使用HttpURLConnection
    private void loadImageWithHttpURLConnection(final String imageUrl, final ImageView imageView) {
        executorService.execute(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestMethod("GET");
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    java.io.InputStream inputStream = connection.getInputStream();
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            Log.d("MainActivity", "HttpURLConnection加载成功 - URL: " + imageUrl);
                        } else {
                            imageView.setImageResource(R.drawable.ic_launcher_background);
                            Log.e("MainActivity", "HttpURLConnection解码失败 - URL: " + imageUrl);
                        }
                    });
                } else {
                    Log.e("MainActivity", "HttpURLConnection请求失败 - URL: " + imageUrl + ", 状态码: " + responseCode);
                    runOnUiThread(() -> imageView.setImageResource(R.drawable.ic_launcher_background));
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("MainActivity", "HttpURLConnection加载失败 - URL: " + imageUrl + ", 错误: " + e.getMessage());
                runOnUiThread(() -> imageView.setImageResource(R.drawable.ic_launcher_background));
            }
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