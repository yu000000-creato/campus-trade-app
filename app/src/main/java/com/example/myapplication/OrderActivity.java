package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.model.OrderResponse;
import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.util.AppExecutors;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class OrderActivity extends AppCompatActivity {

    private LinearLayout llOrders;
    private SwipeRefreshLayout swipeRefresh;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;
    private Integer currentStatus = null; // null 表示全部
    private TextView[] tabs;

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
        swipeRefresh = findViewById(R.id.swipe_refresh);
        apiService = ApiService.getInstance();
        executorService = AppExecutors.getInstance().getNetworkExecutor();

        // 设置下拉刷新
        swipeRefresh.setColorSchemeColors(
                Color.parseColor("#6b46c1"),
                Color.parseColor("#805AD5"),
                Color.parseColor("#9F7AEA")
        );
        swipeRefresh.setOnRefreshListener(this::loadOrders);

        // 初始化状态筛选 Tab
        tabs = new TextView[6];
        tabs[0] = findViewById(R.id.tab_all);
        tabs[1] = findViewById(R.id.tab_pending_payment);
        tabs[2] = findViewById(R.id.tab_pending_shipment);
        tabs[3] = findViewById(R.id.tab_pending_receipt);
        tabs[4] = findViewById(R.id.tab_completed);
        tabs[5] = findViewById(R.id.tab_cancelled);

        // 设置 Tab 点击事件
        tabs[0].setOnClickListener(v -> selectTab(0, null));
        tabs[1].setOnClickListener(v -> selectTab(1, 1));
        tabs[2].setOnClickListener(v -> selectTab(2, 2));
        tabs[3].setOnClickListener(v -> selectTab(3, 3));
        tabs[4].setOnClickListener(v -> selectTab(4, 4));
        tabs[5].setOnClickListener(v -> selectTab(5, 5));
    }

    private void selectTab(int index, Integer status) {
        // 更新 Tab 样式
        for (int i = 0; i < tabs.length; i++) {
            if (i == index) {
                tabs[i].setTextColor(Color.parseColor("#6b46c1"));
                tabs[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tabs[i].setTextColor(Color.parseColor("#666666"));
                tabs[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }

        // 更新当前状态并重新加载订单
        currentStatus = status;
        loadOrders();
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
                String url = "orders/buyer/" + userId + "?page=1&size=20";
                if (currentStatus != null) {
                    url += "&status=" + currentStatus;
                }
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
        swipeRefresh.setRefreshing(false);
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
            tvStatus.setTextColor(getStatusColor(order.getStatus()));
            tvStatus.setPadding(0, 8, 0, 0);

            orderLayout.addView(tvOrderNo);
            orderLayout.addView(tvItem);
            orderLayout.addView(tvPrice);
            orderLayout.addView(tvStatus);

            // 点击查看订单详情
            orderLayout.setOnClickListener(v -> {
                Intent intent = new Intent(OrderActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_id", order.getId());
                startActivity(intent);
            });

            // 添加点击效果
            orderLayout.setBackgroundResource(R.drawable.ripple_white);

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

    private int getStatusColor(int status) {
        switch (status) {
            case 1: return 0xFFE53E3E; // 待付款 - 红色
            case 2: return 0xFFED8936; // 待发货 - 橙色
            case 3: return 0xFF3182CE; // 待收货 - 蓝色
            case 4: return 0xFF38A169; // 已完成 - 绿色
            case 5: return 0xFF718096; // 已取消 - 灰色
            default: return 0xFF6B46C1;
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
    protected void onResume() {
        super.onResume();
        // 从详情页返回时刷新列表
        loadOrders();
    }

    }