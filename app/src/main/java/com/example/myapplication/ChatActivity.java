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
        setContentView(R.layout.activity_chat);

        apiService = ApiService.getInstance();
        userId = getIntent().getLongExtra("userId", 0);
        otherUserId = getIntent().getLongExtra("otherUserId", 0);
        otherUserName = getIntent().getStringExtra("otherUserName");

        getSupportActionBar().setTitle("与 " + otherUserName + " 聊天");

        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        adapter = new ChatAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                Result<List<ChatResponse>> result = apiService.get("chats/conversation/" + userId + "/" + otherUserId, Object.class);
                
                if (result.isSuccess() && result.getData() != null) {
                    Gson gson = new Gson();
                    String dataJson = gson.toJson(result.getData());
                    List<ChatResponse> responses = gson.fromJson(dataJson, 
                        com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, List.class, ChatResponse.class));
                    
                    chatList.clear();
                    for (ChatResponse res : responses) {
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
                String jsonBody = "{\"senderId\": " + userId + ", \"receiverId\": " + otherUserId + ", \"content\": \"" + content + "\"}";
                Result<?> result = apiService.post("chats", jsonBody, Object.class);
                
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