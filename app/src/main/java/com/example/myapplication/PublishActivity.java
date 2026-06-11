package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PublishActivity extends AppCompatActivity {

    private EditText etTitle, etDesc, etOriginalPrice, etCurrentPrice;
    private Spinner spCategory;
    private Button btnPublish;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        initViews();
        loadUserInfo();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_desc);
        etOriginalPrice = findViewById(R.id.et_original_price);
        etCurrentPrice = findViewById(R.id.et_current_price);
        spCategory = findViewById(R.id.sp_category);
        btnPublish = findViewById(R.id.btn_publish);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        btnPublish.setOnClickListener(v -> publishItem());
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);
    }

    private void publishItem() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String originalPriceStr = etOriginalPrice.getText().toString().trim();
        String currentPriceStr = etCurrentPrice.getText().toString().trim();
        int categoryIndex = spCategory.getSelectedItemPosition();

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入商品标题", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPriceStr.isEmpty()) {
            Toast.makeText(this, "请输入商品价格", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPublish.setEnabled(false);

        executorService.execute(() -> {
            try {
                double originalPrice = originalPriceStr.isEmpty() ? 0 : Double.parseDouble(originalPriceStr);
                double currentPrice = Double.parseDouble(currentPriceStr);
                long categoryId = categoryIndex + 1; // 分类从1开始

                String jsonBody = String.format(
                    "{\"title\": \"%s\", \"description\": \"%s\", \"originalPrice\": %f, \"currentPrice\": %f, \"categoryId\": %d}",
                    title, desc, originalPrice, currentPrice, categoryId
                );

                Log.d("PublishActivity", "User ID: " + userId);
                Log.d("PublishActivity", "Full URL: items?userId=" + userId);
                Log.d("PublishActivity", "Request Body: " + jsonBody);
                
                String url = "items?userId=" + userId;
                Result<?> result = apiService.post(url, jsonBody, Object.class);

                runOnUiThread(() -> {
                    btnPublish.setEnabled(true);
                    if (result != null && result.isSuccess()) {
                        Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = "发布失败";
                        if (result != null) {
                            errorMsg = "错误码: " + result.getCode() + ", 信息: " + (result.getMessage() != null ? result.getMessage() : "未知错误");
                        }
                        Toast.makeText(PublishActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (NumberFormatException e) {
                runOnUiThread(() -> {
                    btnPublish.setEnabled(true);
                    Toast.makeText(PublishActivity.this, "请输入正确的价格格式", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("PublishActivity", "Publish error: " + e.getMessage());
                runOnUiThread(() -> {
                    btnPublish.setEnabled(true);
                    Toast.makeText(PublishActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
                });
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