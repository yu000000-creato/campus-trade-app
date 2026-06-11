package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderActivity extends AppCompatActivity {

    private LinearLayout llOrders;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initViews();
        loadUserInfo();
        loadOrders();
    }

    private void initViews() {
        llOrders = findViewById(R.id.ll_orders);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);
    }

    private void loadOrders() {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("orders/buyer/" + userId + "?page=1&size=10");
                Gson gson = new Gson();
                Result<PageResult<Map<String, Object>>> result = 
                    gson.fromJson(jsonResponse, new TypeToken<Result<PageResult<Map<String, Object>>>>() {}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Map<String, Object>> orders = result.getData().getList();
                    runOnUiThread(() -> displayOrders(orders));
                }
            } catch (Exception e) {
                Log.e("OrderActivity", "Load orders error: " + e.getMessage());
            }
        });
    }

    private void displayOrders(List<Map<String, Object>> orders) {
        llOrders.removeAllViews();

        if (orders == null || orders.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("暂无订单");
            tvEmpty.setTextSize(16);
            tvEmpty.setTextColor(0xFF718096);
            tvEmpty.setPadding(0, 40, 0, 40);
            tvEmpty.setGravity(View.TEXT_ALIGNMENT_CENTER);
            llOrders.addView(tvEmpty);
            return;
        }

        for (Map<String, Object> order : orders) {
            LinearLayout orderLayout = new LinearLayout(this);
            orderLayout.setOrientation(LinearLayout.VERTICAL);
            orderLayout.setBackgroundColor(0xFFFFFFFF);
            orderLayout.setPadding(16, 16, 16, 16);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 12);
            orderLayout.setLayoutParams(params);

            TextView tvOrderNo = new TextView(this);
            tvOrderNo.setText("订单号: " + order.get("orderNo"));
            tvOrderNo.setTextSize(14);
            tvOrderNo.setTextColor(0xFF718096);

            TextView tvItem = new TextView(this);
            tvItem.setText("商品: " + order.get("itemTitle"));
            tvItem.setTextSize(16);
            tvItem.setTextColor(0xFF000000);
            tvItem.setPadding(0, 8, 0, 0);

            TextView tvPrice = new TextView(this);
            tvPrice.setText("金额: ¥" + order.get("price"));
            tvPrice.setTextSize(16);
            tvPrice.setTextColor(0xFFFF0000);
            tvPrice.setPadding(0, 8, 0, 0);

            TextView tvStatus = new TextView(this);
            tvStatus.setText("状态: " + getStatusText(((Double) order.get("status")).intValue()));
            tvStatus.setTextSize(14);
            tvStatus.setTextColor(0xFF6B46C1);
            tvStatus.setPadding(0, 8, 0, 0);

            orderLayout.addView(tvOrderNo);
            orderLayout.addView(tvItem);
            orderLayout.addView(tvPrice);
            orderLayout.addView(tvStatus);

            llOrders.addView(orderLayout);
        }
    }

    private String getStatusText(int status) {
        switch (status) {
            case 1: return "待付款";
            case 2: return "已付款";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
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