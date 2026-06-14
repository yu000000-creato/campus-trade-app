package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.OrderResponse;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvStatus, tvStatusDesc, tvItemTitle, tvPrice;
    private TextView tvOrderNo, tvCreatedAt, tvPaymentDeadline;
    private TextView tvBuyerName, tvSellerName, tvAddress, tvRemark, tvPhone;
    private TextView tvCountdown;
    private LinearLayout llPaymentDeadline, llAddress, llRemark, llActions, llPhone;
    private Button btnPay, btnCancel, btnConfirmReceipt, btnContactSeller, btnEditInfo;
    private ImageView ivBack;
    private ApiService apiService;
    private ExecutorService executorService;
    private Long orderId;
    private OrderResponse order;
    private Long userId;
    private Handler countdownHandler;
    private Runnable countdownRunnable;

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
        tvCountdown = findViewById(R.id.tv_countdown);
        tvItemTitle = findViewById(R.id.tv_item_title);
        tvPrice = findViewById(R.id.tv_price);
        tvOrderNo = findViewById(R.id.tv_order_no);
        tvCreatedAt = findViewById(R.id.tv_created_at);
        tvPaymentDeadline = findViewById(R.id.tv_payment_deadline);
        tvBuyerName = findViewById(R.id.tv_buyer_name);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvAddress = findViewById(R.id.tv_address);
        tvPhone = findViewById(R.id.tv_phone);
        tvRemark = findViewById(R.id.tv_remark);
        llPaymentDeadline = findViewById(R.id.ll_payment_deadline);
        llPhone = findViewById(R.id.ll_phone);
        llAddress = findViewById(R.id.ll_address);
        llRemark = findViewById(R.id.ll_remark);
        llActions = findViewById(R.id.ll_actions);
        btnPay = findViewById(R.id.btn_pay);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirmReceipt = findViewById(R.id.btn_confirm_receipt);
        btnContactSeller = findViewById(R.id.btn_contact_seller);
        btnEditInfo = findViewById(R.id.btn_edit_info);
        ivBack = findViewById(R.id.iv_back);

        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        countdownHandler = new Handler(Looper.getMainLooper());

        ivBack.setOnClickListener(v -> finish());
        btnEditInfo.setOnClickListener(v -> showEditInfoDialog());
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

        // 付款截止时间和倒计时
        if (order.getStatus() == 1 && order.getPaymentDeadline() != null) {
            llPaymentDeadline.setVisibility(View.VISIBLE);
            tvPaymentDeadline.setText(formatDateTime(order.getPaymentDeadline()));
            startCountdown(order.getPaymentDeadline());
        } else {
            llPaymentDeadline.setVisibility(View.GONE);
            tvCountdown.setVisibility(View.GONE);
            stopCountdown();
        }

        // 设置买卖双方信息
        tvBuyerName.setText(order.getBuyerName());
        tvSellerName.setText(order.getSellerName());

        // 联系电话
        if (order.getPhone() != null && !order.getPhone().isEmpty()) {
            llPhone.setVisibility(View.VISIBLE);
            tvPhone.setText(order.getPhone());
        } else {
            llPhone.setVisibility(View.GONE);
        }

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

        // 显示编辑按钮（只有买家在待付款状态可以编辑）
        if (order.getStatus() == 1 && order.getBuyerId().equals(userId)) {
            btnEditInfo.setVisibility(View.VISIBLE);
        } else {
            btnEditInfo.setVisibility(View.GONE);
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

    // 倒计时相关方法
    private void startCountdown(String paymentDeadline) {
        stopCountdown();
        tvCountdown.setVisibility(View.VISIBLE);

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown(paymentDeadline);
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void stopCountdown() {
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    private void updateCountdown(String paymentDeadline) {
        try {
            // 使用UTC时间计算，避免时区问题
            LocalDateTime deadline = LocalDateTime.parse(paymentDeadline, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
            long secondsRemaining = ChronoUnit.SECONDS.between(now, deadline);

            if (secondsRemaining <= 0) {
                tvCountdown.setText("订单已超时");
                tvCountdown.setTextColor(0xFF718096);
                stopCountdown();
                // 自动刷新订单状态
                loadOrderDetail();
                return;
            }

            // 格式化显示为 时:分:秒
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            long seconds = secondsRemaining % 60;
            
            String countdownText;
            if (hours > 0) {
                countdownText = String.format("剩余时间: %02d:%02d:%02d", hours, minutes, seconds);
            } else {
                countdownText = String.format("剩余时间: %02d:%02d", minutes, seconds);
            }
            tvCountdown.setText(countdownText);
            tvCountdown.setTextColor(0xFFE53E3E);
        } catch (Exception e) {
            Log.e("OrderDetailActivity", "Countdown error: " + e.getMessage());
            tvCountdown.setVisibility(View.GONE);
        }
    }

    // 编辑地址和电话对话框
    private void showEditInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改地址和电话");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etAddress = new EditText(this);
        etAddress.setHint("请输入收货地址");
        etAddress.setText(order.getAddress() != null ? order.getAddress() : "");
        layout.addView(etAddress);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("请输入联系电话");
        etPhone.setText(order.getPhone() != null ? order.getPhone() : "");
        layout.addView(etPhone);

        builder.setView(layout);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newAddress = etAddress.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();
            updateOrderInfo(newAddress, newPhone);
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateOrderInfo(String address, String phone) {
        executorService.execute(() -> {
            try {
                StringBuilder urlBuilder = new StringBuilder("orders/" + orderId + "/info");
                boolean hasParam = false;
                if (address != null && !address.isEmpty()) {
                    urlBuilder.append("?address=").append(java.net.URLEncoder.encode(address, "UTF-8"));
                    hasParam = true;
                }
                if (phone != null && !phone.isEmpty()) {
                    urlBuilder.append(hasParam ? "&" : "?").append("phone=").append(java.net.URLEncoder.encode(phone, "UTF-8"));
                }

                Result<OrderResponse> result = apiService.put(urlBuilder.toString(), "", OrderResponse.class);

                runOnUiThread(() -> {
                    if (result != null && result.isSuccess()) {
                        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                        loadOrderDetail();
                    } else {
                        Toast.makeText(this, result != null ? result.getMessage() : "修改失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("OrderDetailActivity", "Update order info error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "修改失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
