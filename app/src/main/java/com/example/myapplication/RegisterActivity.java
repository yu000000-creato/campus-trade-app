package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Result;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etRealName, etStudentId, etPhone;
    private Button btnRegister;
    private TextView tvLogin;
    private ApiService apiService;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        etRealName = findViewById(R.id.et_reg_realname);
        etStudentId = findViewById(R.id.et_reg_studentid);
        etPhone = findViewById(R.id.et_reg_phone);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String realName = etRealName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        executorService.execute(() -> {
            try {
                Gson gson = new Gson();
                RegisterRequest request = new RegisterRequest(username, password, realName, studentId, phone);
                String jsonBody = gson.toJson(request);
                
                Result<User> result = apiService.post("users/register", jsonBody, User.class);
                
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    
                    if (result.isSuccess() && result.getData() != null) {
                        Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, 
                                result.getMessage() != null ? result.getMessage() : "注册失败", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("RegisterActivity", "Register error: " + e.getMessage());
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private static class RegisterRequest {
        String username;
        String password;
        String realName;
        String studentId;
        String phone;

        RegisterRequest(String username, String password, String realName, String studentId, String phone) {
            this.username = username;
            this.password = password;
            this.realName = realName.isEmpty() ? null : realName;
            this.studentId = studentId.isEmpty() ? null : studentId;
            this.phone = phone.isEmpty() ? null : phone;
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