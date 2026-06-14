package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Result;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private TextView tvUsername, tvRealName, tvStudentId, tvPhone;
    private ImageView ivAvatar;
    private LinearLayout llLogout;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserInfo();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvRealName = findViewById(R.id.tv_real_name);
        tvStudentId = findViewById(R.id.tv_student_id);
        tvPhone = findViewById(R.id.tv_phone);
        ivAvatar = findViewById(R.id.iv_avatar);
        llLogout = findViewById(R.id.ll_logout);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        llLogout.setOnClickListener(v -> logout());

        findViewById(R.id.ll_my_items).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyItemsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.ll_settings).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        ivAvatar.setOnClickListener(v -> selectImage());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                uploadAvatar(bitmap);
            } catch (IOException e) {
                Log.e("ProfileActivity", "Failed to get image: " + e.getMessage());
                Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadAvatar(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();

        executorService.execute(() -> {
            try {
                String response = apiService.uploadAvatar(userId, imageBytes);
                Gson gson = new Gson();
                Result<User> result = gson.fromJson(response, new TypeToken<Result<User>>() {}.getType());

                runOnUiThread(() -> {
                    if (result != null && result.isSuccess()) {
                        Toast.makeText(ProfileActivity.this, "头像上传成功", Toast.LENGTH_SHORT).show();
                        loadUserInfo();
                    } else {
                        Toast.makeText(ProfileActivity.this, "头像上传失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("ProfileActivity", "Upload avatar error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "头像上传失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);
        String username = prefs.getString("username", "");
        
        tvUsername.setText(username);
        tvRealName.setText(prefs.getString("real_name", ""));
        tvStudentId.setText(prefs.getString("student_id", ""));
        tvPhone.setText(prefs.getString("phone", ""));

        // 从服务器获取最新用户信息
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("users/" + userId);
                Gson gson = new Gson();
                Result<User> result = gson.fromJson(jsonResponse, new TypeToken<Result<User>>() {}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    User user = result.getData();
                    runOnUiThread(() -> {
                        tvRealName.setText(user.getRealName() != null ? user.getRealName() : "");
                        tvStudentId.setText(user.getStudentId() != null ? user.getStudentId() : "");
                        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                        
                        // 更新本地存储
                        getSharedPreferences("campus_trade", MODE_PRIVATE)
                            .edit()
                            .putString("real_name", user.getRealName())
                            .putString("student_id", user.getStudentId())
                            .putString("phone", user.getPhone())
                            .putString("avatar", user.getAvatar())
                            .apply();

                        // 加载头像
                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                .load(user.getAvatar())
                                .circleCrop()
                                .into(ivAvatar);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ProfileActivity", "Load user info error: " + e.getMessage());
            }
        });
    }

    private void logout() {
        getSharedPreferences("campus_trade", MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}