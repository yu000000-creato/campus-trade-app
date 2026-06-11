package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Result;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvRealName, tvStudentId, tvPhone;
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
                            .apply();
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