package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Item;
import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteActivity extends AppCompatActivity {

    private LinearLayout llItems;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initViews();
        loadUserInfo();
        loadFavorites();
    }

    private void initViews() {
        llItems = findViewById(R.id.ll_items);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);
    }

    private void loadFavorites() {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("favorites/user/" + userId + "?page=1&size=10");
                // 使用 ApiService 中配置好的 Gson 实例
                Result<PageResult<Item>> result = apiService.getGson().fromJson(
                    jsonResponse, new TypeToken<Result<PageResult<Item>>>() {}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    runOnUiThread(() -> displayItems(items));
                }
            } catch (Exception e) {
                Log.e("FavoriteActivity", "Load favorites error: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void displayItems(List<Item> items) {
        llItems.removeAllViews();

        if (items == null || items.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("暂无收藏");
            tvEmpty.setTextSize(16);
            tvEmpty.setTextColor(0xFF718096);
            tvEmpty.setPadding(0, 40, 0, 40);
            tvEmpty.setGravity(View.TEXT_ALIGNMENT_CENTER);
            llItems.addView(tvEmpty);
            return;
        }

        for (Item item : items) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setBackgroundColor(0xFFFFFFFF);
            itemLayout.setPadding(12, 12, 12, 12);

            LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemLayoutParams.setMargins(0, 0, 0, 12);
            itemLayout.setLayoutParams(itemLayoutParams);

            TextView imagePlaceholder = new TextView(this);
            imagePlaceholder.setText("❤️");
            imagePlaceholder.setTextSize(48);
            imagePlaceholder.setGravity(View.TEXT_ALIGNMENT_CENTER);
            imagePlaceholder.setBackgroundColor(0xFFfce4ec);

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
            tvTitle.setTextColor(0xFF000000);
            tvTitle.setMaxLines(1);

            TextView tvPrice = new TextView(this);
            tvPrice.setText("¥" + item.getCurrentPrice());
            tvPrice.setTextSize(18);
            tvPrice.setTextColor(0xFFFF0000);

            TextView tvSeller = new TextView(this);
            tvSeller.setText("卖家: " + item.getUsername());
            tvSeller.setTextSize(12);
            tvSeller.setTextColor(0xFF718096);

            infoLayout.addView(tvTitle);
            infoLayout.addView(tvPrice);
            infoLayout.addView(tvSeller);

            itemLayout.addView(imagePlaceholder);
            itemLayout.addView(infoLayout);

            itemLayout.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteActivity.this, ItemDetailActivity.class);
                intent.putExtra("item_id", item.getId());
                startActivity(intent);
            });

            llItems.addView(itemLayout);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}