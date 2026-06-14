package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.OrderResponse;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvStatus, tvStatusDesc, tvItemTitle, tvPrice;
    private TextView tvOrderNo, tvCreatedAt, tvPaymentDeadline;
    private TextView tvBuyerName, tvSellerName, tvAddress, tvRemark;
    private LinearLayout llPaymentDeadline, llAddress, llRemark, llActions;
    private Button btnPay, btnCancel, btnConfirmReceipt, btnContactSeller;
    private ImageView ivBack;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long orderId;
    private OrderResponse order;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        loadOrderId();
        loadOrderDetail();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvStatusDesc = findViewById(R.id.tv_status_desc);
        tvItemTitle = findViewById(R.id.tv_item_title);
        tvPrice = findViewById(R.id.tv_price);
        tvOrderNo = findViewById(R.id.tv_order_no);
        tvCreatedAt = findViewById(R.id.tv_created_at);
        tvPaymentDeadline = findViewById(R.id.tv_payment_deadline);
        tvBuyerName = findViewById(R.id.tv_buyer_name);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvAddress = findViewById(R.id.tv_address);
        tvRemark = findViewById(R.id.tv_remark);
        llPaymentDeadline = findViewById(R.id.ll_payment_deadline);
        llAddress = findViewById(R.id.ll_address);
        llRemark = findViewById(R.id.ll_remark);
        llActions = findViewById(R.id.ll_actions);
        btnPay = findViewById(R.id.btn_pay);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirmReceipt = findViewById(R.id.btn_confirm_receipt);
        btnContactSeller = findViewById(R.id.btn_contact_seller);
        ivBack = findViewById(R.id.iv_back);

        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        ivBack.setOnClickListener(v -> finish());
    }

    private void loadOrderId() {
        orderId = getIntent().getLongExtra("order_id", 0);
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);
    }

    private void loadOrderDetail() {
        if (orderId == 0) {
            Toast.makeText(this, "订单不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("orders/" + orderId);
                Result<OrderResponse> result = apiService.getGson().fromJson(
                    jsonResponse, new TypeToken<Result<OrderResponse>>() {}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    order = result.getData();
                    runOnUiThread(() -> displayOrderDetail(order));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "获取订单详情失败", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e("OrderDetailActivity", "Load order detail error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "获取订单详情失败", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayOrderDetail(OrderResponse order) {
        // 设置状态
        tvStatus.setText(order.getStatusText());
        tvStatus.setTextColor(getStatusColor(order.getStatus()));
        tvStatusDesc.setText(getStatusDesc(order.getStatus()));

        // 设置商品信息
        tvItemTitle.setText(order.getItemTitle());
        tvPrice.setText("¥" + order.getPrice());

        // 设置订单信息
        tvOrderNo.setText(order.getOrderNo());
        tvCreatedAt.setText(formatDateTime(order.getCreatedAt()));

        // 付款截止时间
        if (order.getStatus() == 1 && order.getPaymentDeadline() != null) {
            llPaymentDeadline.setVisibility(View.VISIBLE);
            tvPaymentDeadline.setText(formatDateTime(order.getPaymentDeadline()));
        } else {
            llPaymentDeadline.setVisibility(View.GONE);
        }

        // 设置买卖双方信息
        tvBuyerName.setText(order.getBuyerName());
        tvSellerName.setText(order.getSellerName());

        // 收货地址
        if (order.getAddress() != null && !order.getAddress().isEmpty()) {
            llAddress.setVisibility(View.VISIBLE);
            tvAddress.setText(order.getAddress());
        } else {
            llAddress.setVisibility(View.GONE);
        }

        // 备注
        if (order.getRemark() != null && !order.getRemark().isEmpty()) {
            llRemark.setVisibility(View.VISIBLE);
            tvRemark.setText(order.getRemark());
        } else {
            llRemark.setVisibility(View.GONE);
        }

        // 设置操作按钮
        setupActionButtons(order);
    }

    private void setupActionButtons(OrderResponse order) {
        llActions.setVisibility(View.VISIBLE);

        // 隐藏所有按钮
        btnPay.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnConfirmReceipt.setVisibility(View.GONE);
        btnContactSeller.setVisibility(View.GONE);

        switch (order.getStatus()) {
            case 1: // 待付款
                btnPay.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnContactSeller.setVisibility(View.VISIBLE);

                btnPay.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra("order_id", order.getId());
                    double amountValue = order.getPrice() != null ? order.getPrice().doubleValue() : 0.0;
                    intent.putExtra("amount", amountValue);
                    intent.putExtra("item_count", 1);
                    startActivity(intent);
                });

                btnCancel.setOnClickListener(v -> cancelOrder());
                break;

            case 2: // 待发货
                btnContactSeller.setVisibility(View.VISIBLE);
                break;

            case 3: // 待收货
                btnConfirmReceipt.setVisibility(View.VISIBLE);
                btnContactSeller.setVisibility(View.VISIBLE);

                btnConfirmReceipt.setOnClickListener(v -> confirmReceipt());
                break;

            case 4: // 已完成
            case 5: // 已取消
                llActions.setVisibility(View.GONE);
                break;
        }

        // 联系卖家
        btnContactSeller.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("otherUserId", order.getSellerId());
            intent.putExtra("otherUserName", order.getSellerName());
            startActivity(intent);
        });
    }

    private void cancelOrder() {
        executorService.execute(() -> {
            try {
                String response = apiService.deleteRaw("orders/" + orderId + "/cancel");
                Result<Void> result = apiService.getGson().fromJson(response, Result.class);

                runOnUiThread(() -> {
                    if (result != null && result.isSuccess()) {
                        Toast.makeText(this, "订单已取消", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, result != null ? result.getMessage() : "取消失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "取消失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmReceipt() {
        executorService.execute(() -> {
            try {
                String response = apiService.getRaw("orders/" + orderId + "/status?status=4");
                // 使用 PUT 方法更新状态
                Result<OrderResponse> result = apiService.put("orders/" + orderId + "/status?status=4", "", OrderResponse.class);

                runOnUiThread(() -> {
                    if (result != null && result.isSuccess()) {
                        Toast.makeText(this, "已确认收货", Toast.LENGTH_SHORT).show();
                        loadOrderDetail();
                    } else {
                        Toast.makeText(this, "确认收货失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "确认收货失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
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

    private String getStatusDesc(int status) {
        switch (status) {
            case 1: return "请在30分钟内完成付款";
            case 2: return "等待卖家发货";
            case 3: return "商品正在配送中";
            case 4: return "交易已完成";
            case 5: return "订单已取消";
            default: return "";
        }
    }

    private String formatDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "";
        }
        // 简单格式化，去掉毫秒部分
        if (dateTime.contains(".")) {
            return dateTime.substring(0, dateTime.indexOf(".")).replace("T", " ");
        }
        return dateTime.replace("T", " ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
