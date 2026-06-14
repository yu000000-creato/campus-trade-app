package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Item;
import com.example.myapplication.model.PageResult;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsername;
    private Button btnLogout;
    private EditText etSearch;
    private Button btnSearch;
    private LinearLayout llCategories;
    private RecyclerView rvItems;
    private ItemAdapter itemAdapter;
    private Spinner spinnerSort;
    private SwipeRefreshLayout swipeRefresh;
    private ApiService apiService;
    private ExecutorService executorService;
    private String currentUsername;
    private Long currentCategoryId = null;
    private String currentSort = "time_desc";
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        loadUserInfo();
        loadCategories();
        loadItems();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        btnLogout = findViewById(R.id.btn_logout);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        llCategories = findViewById(R.id.ll_categories);
        rvItems = findViewById(R.id.rv_items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh = findViewById(R.id.swipe_refresh);
        spinnerSort = findViewById(R.id.spinner_sort);
        apiService = ApiService.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        // 设置下拉刷新颜色
        swipeRefresh.setColorSchemeColors(
                Color.parseColor("#667eea"),
                Color.parseColor("#764ba2"),
                Color.parseColor("#6B8DD6")
        );

        // 设置排序选项
        String[] sortOptions = {"最新发布", "价格从低到高", "价格从高到低", "按浏览次数"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());

        // 排序选择监听
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] sortValues = {"time_desc", "price_asc", "price_desc", "view_desc"};
                currentSort = sortValues[position];
                // 重新加载商品
                if (currentCategoryId != null) {
                    filterByCategory(currentCategoryId);
                } else {
                    loadItems();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchItems(keyword);
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchItems(keyword);
            }
            return true;
        });

        findViewById(R.id.tab_favorite).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.tab_publish).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PublishActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.tab_order).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrderActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.tab_profile).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // 设置下拉刷新监听
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            hasMore = true;
            if (currentCategoryId != null) {
                filterByCategory(currentCategoryId);
            } else {
                loadItems();
            }
        });

        // 添加RecyclerView滚动监听，实现自动加载更多
        rvItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();
                    // 当滚动到最后3个item时触发加载更多
                    if (lastVisiblePosition >= totalItemCount - 3 && hasMore && !isLoading) {
                        loadMoreItems();
                    }
                }
            }
        });
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("campus_trade", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "");
        tvUsername.setText("欢迎, " + currentUsername);
    }

    private void logout() {
        getSharedPreferences("campus_trade", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadCategories() {
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("categories");
                Gson gson = new Gson();
                Result<List<Category>> result = gson.fromJson(jsonResponse,
                        new TypeToken<Result<List<Category>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Category> categories = result.getData();
                    runOnUiThread(() -> displayCategories(categories));
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Load categories error: " + e.getMessage());
            }
        });
    }

    private void displayCategories(List<Category> categories) {
        llCategories.removeAllViews();

        // 添加"全部商品"选项
        TextView tvAllItems = new TextView(this);
        tvAllItems.setText("全部商品");
        tvAllItems.setTextSize(14);
        tvAllItems.setTextColor(Color.BLACK);
        tvAllItems.setBackgroundColor(Color.parseColor("#f0f2f5"));
        tvAllItems.setPadding(16, 8, 16, 8);

        LinearLayout.LayoutParams allParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        allParams.setMargins(0, 0, 12, 0);
        tvAllItems.setLayoutParams(allParams);

        tvAllItems.setOnClickListener(v -> {
            currentCategoryId = null;
            loadItems();
        });

        llCategories.addView(tvAllItems);

        for (Category category : categories) {
            TextView tvCategory = new TextView(this);
            tvCategory.setText(category.getName());
            tvCategory.setTextSize(14);
            tvCategory.setTextColor(Color.BLACK);
            tvCategory.setBackgroundColor(Color.parseColor("#f0f2f5"));
            tvCategory.setPadding(16, 8, 16, 8);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 12, 0);
            tvCategory.setLayoutParams(params);

            tvCategory.setOnClickListener(v -> filterByCategory(category.getId()));

            llCategories.addView(tvCategory);
        }
    }

    private void loadItems() {
        currentPage = 1;
        hasMore = true;
        loadItemsByPage(currentPage);
    }

    private void loadItemsByPage(int page) {
        if (isLoading || !hasMore) return;
        
        isLoading = true;
        executorService.execute(() -> {
            try {
                Log.d("MainActivity", "开始加载商品列表，页码: " + page);
                String jsonResponse = apiService.getRaw("items?page=" + page + "&size=10&sort=" + currentSort);
                Log.d("MainActivity", "商品列表响应: " + jsonResponse);
                
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    PageResult<Item> pageResult = result.getData();
                    
                    // 检查是否还有更多数据
                    hasMore = pageResult.getTotal() > page * 10L;
                    
                    Log.d("MainActivity", "成功加载商品数量: " + items.size() + ", 是否还有更多: " + hasMore);
                    
                    runOnUiThread(() -> {
                        if (page == 1) {
                            displayItems(items);
                        } else {
                            addItems(items);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        if (page == 1) {
                            displayItems(null);
                        }
                        hasMore = false;
                    });
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Load items error: " + e.getMessage());
                runOnUiThread(() -> {
                    if (page == 1) {
                        Toast.makeText(MainActivity.this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    hasMore = false;
                });
            } finally {
                isLoading = false;
            }
        });
    }

    private void loadMoreItems() {
        if (currentCategoryId != null) {
            // 如果当前有分类筛选，加载分类下的更多商品
            loadMoreCategoryItems();
        } else {
            // 否则加载全部商品的更多
            currentPage++;
            loadItemsByPage(currentPage);
        }
    }

    private void loadMoreCategoryItems() {
        if (isLoading || !hasMore || currentCategoryId == null) return;
        
        isLoading = true;
        currentPage++;
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("items/category/" + currentCategoryId + "?page=" + currentPage + "&size=10&sort=" + currentSort);
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    PageResult<Item> pageResult = result.getData();
                    hasMore = pageResult.getTotal() > currentPage * 10L;
                    
                    runOnUiThread(() -> addItems(items));
                } else {
                    runOnUiThread(() -> hasMore = false);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Load more category items error: " + e.getMessage());
                runOnUiThread(() -> hasMore = false);
            } finally {
                isLoading = false;
            }
        });
    }

    private void addItems(List<Item> items) {
        if (itemAdapter != null && items != null && !items.isEmpty()) {
            itemAdapter.addItems(items);
        }
    }

    private void searchItems(String keyword) {
        executorService.execute(() -> {
            try {
                String url = "items/search?keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") + "&page=1&size=10&sort=" + currentSort;
                String jsonResponse = apiService.getRaw(url);
                // 使用 ApiService 中配置好的 Gson 实例
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    runOnUiThread(() -> displayItems(items));
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Search items error: " + e.getMessage());
            }
        });
    }

    private void filterByCategory(Long categoryId) {
        currentCategoryId = categoryId;
        currentPage = 1;
        hasMore = true;
        
        executorService.execute(() -> {
            try {
                String jsonResponse = apiService.getRaw("items/category/" + categoryId + "?page=1&size=10&sort=" + currentSort);
                Result<PageResult<Item>> result = apiService.getGson().fromJson(jsonResponse,
                        new TypeToken<Result<PageResult<Item>>>(){}.getType());

                if (result != null && result.isSuccess() && result.getData() != null) {
                    List<Item> items = result.getData().getList();
                    PageResult<Item> pageResult = result.getData();
                    hasMore = pageResult.getTotal() > 10L;
                    runOnUiThread(() -> displayItems(items));
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Filter items error: " + e.getMessage());
            }
        });
    }

    private void displayItems(List<Item> items) {
        swipeRefresh.setRefreshing(false);
        if (items == null || items.isEmpty()) {
            itemAdapter = new ItemAdapter(new ArrayList<>());
            rvItems.setAdapter(itemAdapter);
            return;
        }
        itemAdapter = new ItemAdapter(items);
        rvItems.setAdapter(itemAdapter);
    }

    private class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        private List<Item> items;

        ItemAdapter(List<Item> items) {
            this.items = items != null ? items : new ArrayList<>();
        }

        void addItems(List<Item> newItems) {
            if (newItems == null || newItems.isEmpty()) return;
            int startPosition = items.size();
            items.addAll(newItems);
            notifyItemRangeInserted(startPosition, newItems.size());
        }

        @Override
        public int getItemViewType(int position) {
            if (position == items.size()) {
                return TYPE_FOOTER;
            }
            return TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            // 如果有数据，多加一个footer
            return items.size() > 0 ? items.size() + 1 : 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                View view = getLayoutInflater().inflate(R.layout.item_load_more, parent, false);
                return new FooterViewHolder(view);
            }
            View view = getLayoutInflater().inflate(R.layout.item_home_goods, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                if (hasMore) {
                    footerHolder.progressBar.setVisibility(View.VISIBLE);
                    footerHolder.tvStatus.setText("加载中...");
                } else {
                    footerHolder.progressBar.setVisibility(View.GONE);
                    footerHolder.tvStatus.setText("已到最后");
                }
            } else if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemHolder = (ItemViewHolder) holder;
                Item item = items.get(position);
                itemHolder.tvTitle.setText(item.getTitle());
                itemHolder.tvDesc.setText(item.getDescription() != null ? item.getDescription() : "暂无描述");
                itemHolder.tvPrice.setText("¥" + item.getCurrentPrice());
                itemHolder.tvMeta.setText("卖家: " + item.getUsername() + " | 浏览: " + item.getViewCount());

                String imageUrl = getFirstImageUrl(item.getImages());
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(MainActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .centerCrop()
                            .into(itemHolder.ivImage);
                } else {
                    itemHolder.ivImage.setImageResource(R.drawable.ic_launcher_background);
                }

                itemHolder.itemView.setOnClickListener(v -> showItemDetail(item));
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvTitle, tvDesc, tvPrice, tvMeta;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.iv_image);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvDesc = itemView.findViewById(R.id.tv_desc);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvMeta = itemView.findViewById(R.id.tv_meta);
            }
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {
            ProgressBar progressBar;
            TextView tvStatus;

            FooterViewHolder(View itemView) {
                super(itemView);
                progressBar = itemView.findViewById(R.id.progress_bar);
                tvStatus = itemView.findViewById(R.id.tv_status);
            }
        }
    }

    private String getFirstImageUrl(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return null;
        }
        try {
            List<String> images = new Gson().fromJson(imagesJson, new TypeToken<List<String>>() {}.getType());
            if (images != null && !images.isEmpty()) {
                String imageUrl = images.get(0);
                // 如果是相对路径，添加服务器基础URL
                if (imageUrl.startsWith("/")) {
                    return "http://10.0.2.2:8080" + imageUrl;
                }
                return imageUrl;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Parse images error: " + e.getMessage());
        }
        return null;
    }

    private void showItemDetail(Item item) {
        Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
        intent.putExtra("item_id", item.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}