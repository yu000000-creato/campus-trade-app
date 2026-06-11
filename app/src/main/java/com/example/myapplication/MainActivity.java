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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
                String jsonResponse = apiService.getRaw("items?page=1&size=10");
                Gson gson = new Gson();
                Result<PageResult<Item>> result = gson.fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    runOnUiThread(() -> displayItems(items));
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Load items error: " + e.getMessage());
            }
        });
    }

    private void searchItems(String keyword) {
        executorService.execute(() -> {
            try {
                String url = "items/search?keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") + "&page=1&size=10";
                String jsonResponse = apiService.getRaw(url);
                Gson gson = new Gson();
                Result<PageResult<Item>> result = gson.fromJson(jsonResponse,
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
                Gson gson = new Gson();
                Result<PageResult<Item>> result = gson.fromJson(jsonResponse,
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

            TextView imagePlaceholder = new TextView(this);
            imagePlaceholder.setText("📦");
            imagePlaceholder.setTextSize(48);
            imagePlaceholder.setGravity(Gravity.CENTER);
            imagePlaceholder.setBackgroundColor(Color.parseColor("#e2e8f0"));

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 120);
            imagePlaceholder.setLayoutParams(imageParams);

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

            itemLayout.addView(imagePlaceholder);
            itemLayout.addView(infoLayout);

            itemLayout.setOnClickListener(v -> showItemDetail(item));

            llItems.addView(itemLayout);
        }
    }

    private void showItemDetail(Item item) {
        Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
        intent.putExtra("item_id", item.getId());
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