package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.PaymentResponse;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvOrderId;
    private TextView tvItemCount;
    private TextView tvAmount;
    private RadioGroup rgPaymentMethod;
    private Button btnPay;

    private ApiService apiService;
    private ExecutorService executorService;
    private Gson gson;

    private Long orderId;
    private Double amount;
    private int itemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        loadOrderInfo();
        setupListeners();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tv_order_id);
        tvItemCount = findViewById(R.id.tv_item_count);
        tvAmount = findViewById(R.id.tv_amount);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnPay = findViewById(R.id.btn_pay);

        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        gson = new Gson();

        // 获取订单信息
        Intent intent = getIntent();
        orderId = intent.getLongExtra("order_id", 0);
        amount = intent.getDoubleExtra("amount", 0.0);
        itemCount = intent.getIntExtra("item_count", 1);
    }

    private void loadOrderInfo() {
        tvOrderId.setText("OR" + System.currentTimeMillis());
        tvItemCount.setText(String.valueOf(itemCount));
        tvAmount.setText("¥" + String.format("%.2f", amount));
    }

    private void setupListeners() {
        btnPay.setOnClickListener(v -> {
            String paymentMethod = getSelectedPaymentMethod();
            Log.d("PaymentActivity", "选择支付方式: " + paymentMethod);
            makePayment(paymentMethod);
        });

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private String getSelectedPaymentMethod() {
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_wechat) {
            return "wechat";
        } else if (selectedId == R.id.rb_alipay) {
            return "alipay";
        } else {
            return "balance";
        }
    }

    private void makePayment(String paymentMethod) {
        btnPay.setEnabled(false);
        btnPay.setText("支付中...");

        executorService.execute(() -> {
            try {
                // 构建支付请求
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("orderId", orderId);
                requestBody.put("paymentMethod", paymentMethod);
                requestBody.put("amount", amount);

                String jsonRequest = gson.toJson(requestBody);
                Log.d("PaymentActivity", "支付请求: " + jsonRequest);

                // 调用支付接口
                Result<PaymentResponse> result = apiService.<PaymentResponse>post("payment/create", jsonRequest, PaymentResponse.class);
                Log.d("PaymentActivity", "支付响应: " + gson.toJson(result));

                runOnUiThread(() -> {
                    btnPay.setEnabled(true);
                    btnPay.setText("确认支付");

                    if (result.isSuccess()) {
                        Toast.makeText(PaymentActivity.this, "支付成功!", Toast.LENGTH_SHORT).show();
                        // 返回订单列表页面
                        Intent intent = new Intent(PaymentActivity.this, OrderActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "支付失败: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("PaymentActivity", "支付失败", e);
                runOnUiThread(() -> {
                    btnPay.setEnabled(true);
                    btnPay.setText("确认支付");
                    Toast.makeText(PaymentActivity.this, "支付失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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