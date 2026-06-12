package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.OrderResponse;
import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.reflect.TypeToken;

import java.util.List;
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
        Log.d("OrderActivity", "Loaded userId from shared preferences: " + userId);
        
        // 测试用：如果没有登录，默认使用用户ID 2（张三）来查看订单
        if (userId == 0) {
            userId = 2L;
            Log.d("OrderActivity", "No user logged in, using default userId: 2");
        }
    }

    private void loadOrders() {
        executorService.execute(() -> {
            try {
                Log.d("OrderActivity", "Current userId: " + userId);
                String url = "orders/buyer/" + userId + "?page=1&size=10";
                Log.d("OrderActivity", "Request URL: " + url);
                
                String jsonResponse = apiService.getRaw(url);
                Log.d("OrderActivity", "API Response: " + jsonResponse);
                
                // 使用 ApiService 中配置好的 Gson 实例
                Result<PageResult<OrderResponse>> result = apiService.getGson().fromJson(
                    jsonResponse, new TypeToken<Result<PageResult<OrderResponse>>>() {}.getType());

                Log.d("OrderActivity", "Result success: " + (result != null && result.isSuccess()));
                
                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<OrderResponse> orders = result.getData().getList();
                    Log.d("OrderActivity", "Order count: " + orders.size());
                    runOnUiThread(() -> displayOrders(orders));
                } else {
                    runOnUiThread(() -> displayOrders(null));
                }
            } catch (Exception e) {
                Log.e("OrderActivity", "Load orders error: " + e.getMessage());
                runOnUiThread(() -> displayOrders(null));
            }
        });
    }

    private void displayOrders(List<OrderResponse> orders) {
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

        for (OrderResponse order : orders) {
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
            tvOrderNo.setText("订单号: " + order.getOrderNo());
            tvOrderNo.setTextSize(14);
            tvOrderNo.setTextColor(0xFF718096);

            TextView tvItem = new TextView(this);
            tvItem.setText("商品: " + order.getItemTitle());
            tvItem.setTextSize(16);
            tvItem.setTextColor(0xFF000000);
            tvItem.setPadding(0, 8, 0, 0);

            TextView tvPrice = new TextView(this);
            tvPrice.setText("金额: ¥" + order.getPrice());
            tvPrice.setTextSize(16);
            tvPrice.setTextColor(0xFFFF0000);
            tvPrice.setPadding(0, 8, 0, 0);

            TextView tvStatus = new TextView(this);
            tvStatus.setText("状态: " + order.getStatusText());
            tvStatus.setTextSize(14);
            tvStatus.setTextColor(0xFF6B46C1);
            tvStatus.setPadding(0, 8, 0, 0);

            orderLayout.addView(tvOrderNo);
            orderLayout.addView(tvItem);
            orderLayout.addView(tvPrice);
            orderLayout.addView(tvStatus);

            // 如果订单状态是待付款，添加付款按钮和截止时间
            if (order.getStatus() == 1) {
                TextView tvDeadline = new TextView(this);
                String deadlineText = order.getPaymentDeadline() != null ? order.getPaymentDeadline() : "30分钟内";
                tvDeadline.setText("请在 " + deadlineText + " 内完成付款");
                tvDeadline.setTextSize(12);
                tvDeadline.setTextColor(0xFFE53E3E);
                tvDeadline.setPadding(0, 8, 0, 0);
                orderLayout.addView(tvDeadline);
                Button btnPay = new Button(this);
                btnPay.setText("立即付款");
                btnPay.setBackgroundColor(0xFF6B46C1);
                btnPay.setTextColor(0xFFFFFFFF);
                btnPay.setTextSize(16);
                btnPay.setPadding(16, 0, 16, 0);
                
                // 设置固定高度确保两个按钮平齐
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(48) // 固定高度48dp
                );
                btnParams.setMargins(0, 12, 0, 0);
                btnPay.setLayoutParams(btnParams);
                
                btnPay.setOnClickListener(v -> {
                    // 跳转到支付页面
                    Intent intent = new Intent(OrderActivity.this, PaymentActivity.class);
                    intent.putExtra("order_id", order.getId());
                    // 将 BigDecimal 转换为 double 传递
                    double amountValue = order.getPrice() != null ? order.getPrice().doubleValue() : 0.0;
                    intent.putExtra("amount", amountValue);
                    intent.putExtra("item_count", 1);
                    startActivity(intent);
                });
                
                // 创建水平布局容器放置两个按钮
                LinearLayout buttonLayout = new LinearLayout(this);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                buttonLayoutParams.setMargins(0, 12, 0, 0);
                buttonLayout.setLayoutParams(buttonLayoutParams);
                
                buttonLayout.addView(btnPay);
                
                // 添加取消订单按钮
                Button btnCancel = new Button(this);
                btnCancel.setText("取消订单");
                btnCancel.setBackgroundColor(0xFFE53E3E);
                btnCancel.setTextColor(0xFFFFFFFF);
                btnCancel.setTextSize(16);
                btnCancel.setPadding(16, 0, 16, 0);
                
                // 设置固定高度确保两个按钮平齐
                LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(48) // 固定高度48dp
                );
                cancelParams.setMargins(16, 0, 0, 0); // 只设置左边距
                btnCancel.setLayoutParams(cancelParams);
                
                btnCancel.setOnClickListener(v -> {
                    cancelOrder(order.getId());
                });
                
                buttonLayout.addView(btnCancel);
                
                orderLayout.addView(buttonLayout);
            }

            llOrders.addView(orderLayout);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
    
    private String getStatusText(int status) {
        switch (status) {
            case 1: return "待付款";
            case 2: return "已付款";
            case 3: return "待收货";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }

    private void cancelOrder(Long orderId) {
        executorService.execute(() -> {
            try {
                String response = apiService.deleteRaw("orders/" + orderId + "/cancel");
                Result<Void> result = apiService.getGson().fromJson(response, Result.class);
                
                if (result != null && result.isSuccess()) {
                    runOnUiThread(() -> {
                        Toast.makeText(OrderActivity.this, "订单已取消", Toast.LENGTH_SHORT).show();
                        loadOrders(); // 刷新订单列表
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(OrderActivity.this, 
                                result != null ? result.getMessage() : "取消失败", 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("OrderActivity", "Cancel order error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(OrderActivity.this, "取消失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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