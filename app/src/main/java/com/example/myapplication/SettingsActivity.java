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

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout llEditProfile, llChangePassword, llAbout;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadUserInfo();
    }

    private void initViews() {
        llEditProfile = findViewById(R.id.ll_edit_profile);
        llChangePassword = findViewById(R.id.ll_change_password);
        llAbout = findViewById(R.id.ll_about);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);

        llEditProfile.setOnClickListener(v -> showEditProfile());
        llChangePassword.setOnClickListener(v -> showChangePassword());
        llAbout.setOnClickListener(v -> showAbout());
    }

    private void loadUserInfo() {
        // 可以在这里加载用户信息
    }

    private void showEditProfile() {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("users/" + userId);
                Gson gson = new Gson();
                Result<User> result = gson.fromJson(jsonResponse, new TypeToken<Result<User>>() {}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    User user = result.getData();
                    runOnUiThread(() -> {
                        // 显示编辑资料对话框
                        showEditProfileDialog(user);
                    });
                }
            } catch (Exception e) {
                Log.e("SettingsActivity", "Load user info error: " + e.getMessage());
            }
        });
    }

    private void showEditProfileDialog(User user) {
        // 创建编辑资料对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("编辑资料");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextView etRealName = view.findViewById(R.id.et_real_name);
        TextView etStudentId = view.findViewById(R.id.et_student_id);
        TextView etPhone = view.findViewById(R.id.et_phone);

        etRealName.setText(user.getRealName() != null ? user.getRealName() : "");
        etStudentId.setText(user.getStudentId() != null ? user.getStudentId() : "");
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");

        builder.setView(view);
        builder.setPositiveButton("保存", (dialog, which) -> {
            String realName = etRealName.getText().toString().trim();
            String studentId = etStudentId.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            updateProfile(realName, studentId, phone);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void updateProfile(String realName, String studentId, String phone) {
        executorService.execute(() -> {
            try {
                String jsonBody = String.format("{\"realName\":\"%s\",\"studentId\":\"%s\",\"phone\":\"%s\"}", 
                        realName, studentId, phone);
                Result<User> result = apiService.put("users/" + userId, jsonBody, User.class);

                runOnUiThread(() -> {
                    if (result != null && result.isSuccess()) {
                        Toast.makeText(SettingsActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        // 更新本地存储
                        getSharedPreferences("campus_trade", MODE_PRIVATE)
                            .edit()
                            .putString("real_name", realName)
                            .putString("student_id", studentId)
                            .putString("phone", phone)
                            .apply();
                    } else {
                        Toast.makeText(SettingsActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("SettingsActivity", "Update profile error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showChangePassword() {
        // 创建修改密码对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("修改密码");

        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextView etOldPassword = view.findViewById(R.id.et_old_password);
        TextView etNewPassword = view.findViewById(R.id.et_new_password);
        TextView etConfirmPassword = view.findViewById(R.id.et_confirm_password);

        builder.setView(view);
        builder.setPositiveButton("确认修改", (dialog, which) -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (newPassword.length() < 6) {
                Toast.makeText(SettingsActivity.this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(SettingsActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(oldPassword, newPassword);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void changePassword(String oldPassword, String newPassword) {
        executorService.execute(() -> {
            try {
                String jsonBody = String.format("{\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}", 
                        oldPassword, newPassword);
                Result<?> result = apiService.post("users/change-password", jsonBody, Object.class);

                runOnUiThread(() -> {
                    if (result != null && result.isSuccess()) {
                        Toast.makeText(SettingsActivity.this, "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                        // 退出登录
                        getSharedPreferences("campus_trade", MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply();
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = result != null && result.getMessage() != null ? result.getMessage() : "修改失败";
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("SettingsActivity", "Change password error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showAbout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("关于我们");
        builder.setMessage("校园二手交易平台 v1.1.0\n\n致力于为校园师生提供便捷的二手物品交易服务。");
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}