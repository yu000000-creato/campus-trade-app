INSERT INTO users (username, password, real_name, student_id, phone, email) VALUES
('zhangsan', '123456', '张三', '2021001', '13800138001', 'zhangsan@example.com'),
('lisi', '123456', '李四', '2021002', '13800138002', 'lisi@example.com'),
('wangwu', '123456', '王五', '2021003', '13800138003', 'wangwu@example.com');

INSERT INTO items (user_id, category_id, title, description, original_price, current_price, images, status) VALUES
(1, 1, 'iPhone 14 Pro 256G', '自用iPhone 14 Pro，成色95新，无磕碰划痕，电池健康度92%，配件齐全', 8999.00, 5999.00, '["https://example.com/iphone1.jpg","https://example.com/iphone2.jpg"]', 1),
(1, 3, '小米台灯Pro', '护眼台灯，亮度可调，使用半年，功能正常', 299.00, 150.00, '["https://example.com/lamp1.jpg"]', 1),
(2, 2, '高等数学教材全套', '同济第七版高等数学上下册，附带习题册，有少量笔记', 120.00, 50.00, '["https://example.com/book1.jpg"]', 1),
(2, 4, '羽毛球拍套装', '尤尼克斯羽毛球拍，送3个球，手胶已换新', 399.00, 200.00, '["https://example.com/racket1.jpg"]', 1),
(3, 5, '耐克运动鞋', 'Air Force 1，42码，穿过几次，几乎全新', 799.00, 450.00, '["https://example.com/shoes1.jpg","https://example.com/shoes2.jpg"]', 1);