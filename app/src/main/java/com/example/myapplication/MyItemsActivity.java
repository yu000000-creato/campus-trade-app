package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.model.Item;
import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.reflect.TypeToken;

import com.example.myapplication.util.AppExecutors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MyItemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ItemAdapter adapter;
    private List<Item> itemList = new ArrayList<>();
    private ApiService apiService;
    private ExecutorService executorService;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_items);

        initViews();
        loadMyItems();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tv_empty);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        apiService = ApiService.getInstance();
        executorService = AppExecutors.getInstance().getNetworkExecutor();

        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);

        adapter = new ItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 设置下拉刷新
        swipeRefresh.setColorSchemeColors(
                Color.parseColor("#667eea"),
                Color.parseColor("#764ba2"),
                Color.parseColor("#6B8DD6")
        );
        swipeRefresh.setOnRefreshListener(this::loadMyItems);
    }

    private void loadMyItems() {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("items/user/" + userId + "?page=1&size=20");
                Log.d("MyItemsActivity", "Response: " + jsonResponse);
                
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse, 
                        new TypeToken<Result<PageResult<Item>>>() {}.getType());

                if (result != null && result.isSuccess() && result.getData() != null && result.getData().getList() != null) {
                    itemList.clear();
                    itemList.addAll(result.getData().getList());
                    runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerView.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
                    });
                } else {
                    runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                Log.e("MyItemsActivity", "Load items error: " + e.getMessage());
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
            }
        });
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_goods, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Item item = itemList.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvPrice.setText("¥" + item.getCurrentPrice());
            holder.tvCategory.setText(getCategoryName(item.getCategoryId()));
            
            if (item.getOriginalPrice() != null && item.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                holder.tvOriginalPrice.setText("原价: ¥" + item.getOriginalPrice());
            } else {
                holder.tvOriginalPrice.setText("");
            }

            // 设置商品状态
            Integer status = item.getStatus();
            if (status != null) {
                if (status == 1) {
                    holder.tvStatus.setText("上架中");
                    holder.tvStatus.setTextColor(0xFF48BB78);
                    holder.btnAction.setText("下架");
                    holder.btnAction.setBackgroundColor(0xFFE53E3E);
                    holder.btnAction.setOnClickListener(v -> toggleItemStatus(item.getId(), 2));
                } else if (status == 2) {
                    holder.tvStatus.setText("已下架");
                    holder.tvStatus.setTextColor(0xFFA0AEC0);
                    holder.btnAction.setText("上架");
                    holder.btnAction.setBackgroundColor(0xFF48BB78);
                    holder.btnAction.setOnClickListener(v -> toggleItemStatus(item.getId(), 1));
                } else {
                    holder.tvStatus.setText("状态未知");
                    holder.tvStatus.setTextColor(0xFF718096);
                    holder.btnAction.setText("上架");
                    holder.btnAction.setBackgroundColor(0xFF718096);
                    holder.btnAction.setOnClickListener(v -> toggleItemStatus(item.getId(), 1));
                }
            } else {
                holder.tvStatus.setText("状态未知");
                holder.tvStatus.setTextColor(0xFF718096);
                holder.btnAction.setText("上架");
                holder.btnAction.setBackgroundColor(0xFF48BB78);
                holder.btnAction.setOnClickListener(v -> toggleItemStatus(item.getId(), 1));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MyItemsActivity.this, ItemDetailActivity.class);
                intent.putExtra("item_id", item.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvOriginalPrice, tvCategory, tvStatus;
            TextView btnAction;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
                tvCategory = itemView.findViewById(R.id.tv_category);
                tvStatus = itemView.findViewById(R.id.tv_status);
                btnAction = itemView.findViewById(R.id.btn_action);
            }
        }
    }

    // 切换商品上下架状态
    private void toggleItemStatus(Long itemId, int targetStatus) {
        executorService.execute(() -> {
            try {
                String jsonBody = "{\"status\": " + targetStatus + "}";
                Result<Void> result = apiService.put("items/" + itemId + "/status", jsonBody, Void.class);
                
                if (result != null && result.isSuccess()) {
                    runOnUiThread(() -> {
                        String message = targetStatus == 1 ? "上架成功" : "下架成功";
                        Toast.makeText(MyItemsActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadMyItems(); // 刷新商品列表
                    });
                } else {
                    runOnUiThread(() -> {
                        String message = targetStatus == 1 ? "上架失败" : "下架失败";
                        String errorMsg = result != null ? result.getMessage() : message;
                        Toast.makeText(MyItemsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("MyItemsActivity", "Toggle status error: " + e.getMessage());
                runOnUiThread(() -> {
                    String message = targetStatus == 1 ? "上架失败: " + e.getMessage() : "下架失败: " + e.getMessage();
                    Toast.makeText(MyItemsActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getCategoryName(Long categoryId) {
        String[] categories = {"", "数码产品", "图书教材", "生活用品", "运动器材", "服装配饰", "其他物品"};
        if (categoryId != null && categoryId > 0 && categoryId < categories.length) {
            return categories[categoryId.intValue()];
        }
        return "未知分类";
    }

    }