package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.Item;
import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.reflect.TypeToken;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyItemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
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
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 0);

        adapter = new ItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerView.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
                    });
                } else {
                    runOnUiThread(() -> {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                Log.e("MyItemsActivity", "Load items error: " + e.getMessage());
                runOnUiThread(() -> {
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
                    holder.tvStatus.setText("状态: 上架中");
                    holder.tvStatus.setTextColor(0xFF48BB78);
                } else if (status == 2) {
                    holder.tvStatus.setText("状态: 已下架");
                    holder.tvStatus.setTextColor(0xFFA0AEC0);
                } else {
                    holder.tvStatus.setText("状态: 未知");
                    holder.tvStatus.setTextColor(0xFF718096);
                }
            } else {
                holder.tvStatus.setText("");
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

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
                tvCategory = itemView.findViewById(R.id.tv_category);
                tvStatus = itemView.findViewById(R.id.tv_status);
            }
        }
    }

    private String getCategoryName(Long categoryId) {
        String[] categories = {"", "数码产品", "图书教材", "生活用品", "运动器材", "服装配饰", "其他物品"};
        if (categoryId != null && categoryId > 0 && categoryId < categories.length) {
            return categories[categoryId.intValue()];
        }
        return "未知分类";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}