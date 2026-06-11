package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.example.myapplication.model.ChatResponse;
import com.example.myapplication.model.Result;
import com.example.myapplication.network.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private ApiService apiService;
    private long userId;
    private long otherUserId;
    private String otherUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_chat);

            apiService = ApiService.getInstance();
            userId = getIntent().getLongExtra("userId", 0);
            otherUserId = getIntent().getLongExtra("otherUserId", 0);
            otherUserName = getIntent().getStringExtra("otherUserName");

            Log.d("ChatActivity", "userId from intent: " + userId);
            Log.d("ChatActivity", "otherUserId from intent (otherUserId): " + otherUserId);
            Log.d("ChatActivity", "otherUserName from intent (otherUserName): " + otherUserName);

            // 支持从商品详情页跳转过来的参数
            if (otherUserId == 0) {
                otherUserId = getIntent().getLongExtra("sellerId", 0);
                otherUserName = getIntent().getStringExtra("sellerName");
                Log.d("ChatActivity", "otherUserId from intent (sellerId): " + otherUserId);
                Log.d("ChatActivity", "otherUserName from intent (sellerName): " + otherUserName);
            }

            // 检查参数是否有效
            if (userId == 0) {
                Toast.makeText(this, "用户ID为空，请先登录", Toast.LENGTH_SHORT).show();
                Log.e("ChatActivity", "userId is 0");
                finish();
                return;
            }
            
            if (otherUserId == 0) {
                Toast.makeText(this, "卖家ID为空", Toast.LENGTH_SHORT).show();
                Log.e("ChatActivity", "otherUserId is 0");
                finish();
                return;
            }
            
            if (otherUserName == null) {
                otherUserName = "卖家";
            }

            recyclerView = findViewById(R.id.recyclerView);
            etMessage = findViewById(R.id.et_message);
            btnSend = findViewById(R.id.btn_send);

            adapter = new ChatAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            btnSend.setOnClickListener(v -> sendMessage());

            loadMessages();
            Log.d("ChatActivity", "ChatActivity created successfully");
        } catch (Exception e) {
            Log.e("ChatActivity", "Error creating ChatActivity: " + e.getMessage(), e);
            Toast.makeText(this, "聊天页面加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                Result<List<ChatResponse>> result = apiService.getList("chats/conversation/" + userId + "/" + otherUserId, ChatResponse.class);
                
                if (result.isSuccess() && result.getData() != null) {
                    chatList.clear();
                    for (ChatResponse res : result.getData()) {
                        chatList.add(new ChatItem(res.getSenderId(), res.getContent()));
                    }
                }
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(chatList.size() - 1);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        new Thread(() -> {
            try {
                String jsonBody = "{\"receiverId\": " + otherUserId + ", \"content\": \"" + content + "\"}";
                Result<?> result = apiService.post("chats/" + userId, jsonBody, Object.class);
                
                if (result.isSuccess()) {
                    chatList.add(new ChatItem(userId, content));
                    new Handler(Looper.getMainLooper()).post(() -> {
                        etMessage.setText("");
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(chatList.size() - 1);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutId = viewType == 0 ? R.layout.item_chat_sent : R.layout.item_chat_received;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ChatItem item = chatList.get(position);
            holder.tvContent.setText(item.content);
        }

        @Override
        public int getItemViewType(int position) {
            return chatList.get(position).senderId == userId ? 0 : 1;
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent;

            ViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tv_content);
            }
        }
    }

    private static class ChatItem {
        long senderId;
        String content;

        ChatItem(long senderId, String content) {
            this.senderId = senderId;
            this.content = content;
        }
    }

    public static class ChatResponse {
        private Long senderId;
        private String content;

        public Long getSenderId() { return senderId; }
        public String getContent() { return content; }
    }
}