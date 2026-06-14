package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String EXTRA_CURRENT_INDEX = "current_index";

    private ImageView ivPreview;
    private TextView tvImageCount;
    private Button btnClose;
    private List<String> imageUrls;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        initViews();
        loadImage();
    }

    private void initViews() {
        ivPreview = findViewById(R.id.iv_preview);
        tvImageCount = findViewById(R.id.tv_image_count);
        btnClose = findViewById(R.id.btn_close);

        btnClose.setOnClickListener(v -> finish());

        // 获取传入的图片数据
        String singleUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        currentIndex = getIntent().getIntExtra(EXTRA_CURRENT_INDEX, 0);

        if (urls != null && !urls.isEmpty()) {
            imageUrls = urls;
        } else if (singleUrl != null) {
            imageUrls = new ArrayList<>();
            imageUrls.add(singleUrl);
            currentIndex = 0;
        } else {
            imageUrls = new ArrayList<>();
        }

        // 设置点击图片切换下一张
        ivPreview.setOnClickListener(v -> {
            if (imageUrls.size() > 1) {
                currentIndex = (currentIndex + 1) % imageUrls.size();
                loadImage();
            }
        });
    }

    private void loadImage() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            finish();
            return;
        }

        String imageUrl = imageUrls.get(currentIndex);
        
        // 处理相对路径
        if (imageUrl.startsWith("/")) {
            imageUrl = "http://10.0.2.2:8080" + imageUrl;
        }

        Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.ic_launcher_background)
                .into(ivPreview);

        // 更新计数显示
        tvImageCount.setText((currentIndex + 1) + " / " + imageUrls.size());
    }
}
