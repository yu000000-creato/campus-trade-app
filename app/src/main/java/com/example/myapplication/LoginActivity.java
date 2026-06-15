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

import com.example.myapplication.util.AppExecutors;

import java.util.concurrent.ExecutorService;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ApiService apiService;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        apiService = ApiService.getInstance();
        executorService = AppExecutors.getInstance().getNetworkExecutor();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        executorService.execute(() -> {
            try {
                Log.d("LoginActivity", "Attempting login with username: " + username);
                
                Gson gson = new Gson();
                String jsonBody = gson.toJson(new LoginRequest(username, password));
                Log.d("LoginActivity", "Request body: " + jsonBody);
                
                Result<User> result = apiService.post("users/login", jsonBody, User.class);
                Log.d("LoginActivity", "Result code: " + result.getCode() + ", message: " + result.getMessage());
                
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    
                    if (result.isSuccess() && result.getData() != null) {
                        User user = result.getData();
                        saveUser(user);
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = result.getMessage() != null ? result.getMessage() : "登录失败";
                        Log.e("LoginActivity", "Login failed: " + errorMsg);
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("LoginActivity", "Login exception: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    String errorMsg = "网络异常: " + e.getMessage();
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveUser(User user) {
        getSharedPreferences("campus_trade", MODE_PRIVATE)
                .edit()
                .putLong("user_id", user.getId())
                .putString("username", user.getUsername())
                .putString("real_name", user.getRealName())
                .putString("phone", user.getPhone())
                .putBoolean("is_logged_in", true)
                .apply();
    }

    private static class LoginRequest {
        String username;
        String password;

        LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    }