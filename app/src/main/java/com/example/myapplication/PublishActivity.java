package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.util.AppExecutors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class PublishActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etTitle, etDesc, etOriginalPrice, etCurrentPrice;
    private Spinner spCategory;
    private Button btnPublish, btnSelectImage;
    private ImageView ivPreview;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;
    private String uploadedImageUrl;

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
        btnSelectImage = findViewById(R.id.btn_select_image);
        ivPreview = findViewById(R.id.iv_preview);
        apiService = ApiService.getInstance();
        executorService = AppExecutors.getInstance().getNetworkExecutor();

        btnPublish.setOnClickListener(v -> publishItem());
        btnSelectImage.setOnClickListener(v -> selectImage());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivPreview.setImageBitmap(bitmap);
                ivPreview.setVisibility(View.VISIBLE);
                uploadImage(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage(Bitmap bitmap) {
        executorService.execute(() -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageBytes = stream.toByteArray();

                String response = apiService.uploadImage(imageBytes);
                Log.d("PublishActivity", "Upload response: " + response);

                // 解析返回的 JSON 获取图片 URL
                Gson gson = new Gson();
                Result<String> result = gson.fromJson(response, new TypeToken<Result<String>>(){}.getType());
                if (result != null && result.isSuccess()) {
                    uploadedImageUrl = result.getData();
                    runOnUiThread(() -> Toast.makeText(PublishActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(PublishActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("PublishActivity", "Upload image error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(PublishActivity.this, "图片上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
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

                String jsonBody;
                if (uploadedImageUrl != null) {
                    jsonBody = String.format(
                        "{\"title\": \"%s\", \"description\": \"%s\", \"originalPrice\": %f, \"currentPrice\": %f, \"categoryId\": %d, \"images\": \"%s\"}",
                        title, desc, originalPrice, currentPrice, categoryId, uploadedImageUrl
                    );
                } else {
                    jsonBody = String.format(
                        "{\"title\": \"%s\", \"description\": \"%s\", \"originalPrice\": %f, \"currentPrice\": %f, \"categoryId\": %d}",
                        title, desc, originalPrice, currentPrice, categoryId
                    );
                }

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

    }