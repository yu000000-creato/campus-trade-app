INSERT INTO users (username, password, real_name, student_id, phone, email) VALUES
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', '2021001', '13800138001', 'zhangsan@example.com'),
('lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', '2021002', '13800138002', 'lisi@example.com'),
('wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', '2021003', '13800138003', 'wangwu@example.com'),
('zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', '赵六', '2021004', '13800138004', 'zhaoliu@example.com'),
('sunqi', 'e10adc3949ba59abbe56e057f20f883e', '孙七', '2021005', '13800138005', 'sunqi@example.com'),
('zhouba', 'e10adc3949ba59abbe56e057f20f883e', '周八', '2021006', '13800138006', 'zhouba@example.com');

INSERT INTO items (user_id, username, category_id, title, description, original_price, current_price, images, status) VALUES
(1, 'zhangsan', 1, 'iPhone 14 Pro 256G', '自用iPhone 14 Pro，成色95新，无磕碰划痕，电池健康度92%，配件齐全', 8999.00, 5999.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=iPhone%2014%20Pro%20smartphone%20on%20white%20background&image_size=square"]', 1),
(1, 'zhangsan', 1, 'MacBook Pro 14寸', 'M2 Pro芯片，16GB内存，512GB存储，保修到2025年', 16999.00, 12000.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=MacBook%20Pro%20laptop%20on%20desk&image_size=square"]', 1),
(1, 'zhangsan', 3, '小米台灯Pro', '护眼台灯，亮度可调，使用半年，功能正常', 299.00, 150.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=modern%20desk%20lamp%20LED&image_size=square"]', 1),
(2, 'lisi', 2, '高等数学教材全套', '同济第七版高等数学上下册，附带习题册，有少量笔记', 120.00, 50.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=math%20textbooks%20stack&image_size=square"]', 1),
(2, 'lisi', 2, 'Java编程思想', '第四版，经典Java学习书籍，几乎全新', 108.00, 45.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=Java%20programming%20book&image_size=square"]', 1),
(2, 'lisi', 4, '羽毛球拍套装', '尤尼克斯羽毛球拍，送3个球，手胶已换新', 399.00, 200.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=badminton%20racket%20set&image_size=square"]', 1),
(3, 'wangwu', 5, '耐克运动鞋', 'Air Force 1，42码，穿过几次，几乎全新', 799.00, 450.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=Nike%20Air%20Force%201%20shoes&image_size=square"]', 1),
(3, 'wangwu', 5, '运动背包', '户外登山背包，容量40L，防水材质', 350.00, 180.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=hiking%20backpack%20outdoor&image_size=square"]', 1),
(4, 'zhaoliu', 6, '吉他', '雅马哈F310，原木色，赠送吉他包和调音器', 899.00, 500.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=acoustic%20guitar%20Yamaha&image_size=square"]', 1),
(4, 'zhaoliu', 1, 'iPad Air 5', '10.9寸，M1芯片，256GB，Wi-Fi版', 5999.00, 4200.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=iPad%20Air%20tablet%20on%20couch&image_size=square"]', 1),
(5, 'sunqi', 3, '加湿器', '小熊加湿器，大容量，静音设计', 199.00, 90.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=humidifier%20home%20appliance&image_size=square"]', 1),
(5, 'sunqi', 2, '考研英语真题', '历年考研英语一真题，附带答案解析', 68.00, 30.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=exam%20preparation%20books&image_size=square"]', 1),
(6, 'zhouba', 4, '篮球', '斯伯丁篮球，室外用球，手感好', 199.00, 100.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=basketball%20Spalding%20orange&image_size=square"]', 1),
(6, 'zhouba', 5, '羽绒服', '波司登羽绒服，L码，黑色，穿过一个冬天', 899.00, 400.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=black%20down%20jacket%20winter&image_size=square"]', 1),
(1, 'zhangsan', 6, '电子琴', '卡西欧电子琴，61键，带琴架', 1200.00, 650.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=electronic%20keyboard%20Casio&image_size=square"]', 1),
(2, 'lisi', 3, '电热水壶', '美的电热水壶，1.7L，快速烧水', 129.00, 60.00, '["https://neeko-copilot.bytedance.net/api/text_to_image?prompt=electric%20kettle%20stainless%20steel&image_size=square"]', 1);

INSERT INTO orders (user_id, item_id, status, address, remark, total_price) VALUES
(2, 1, 2, '男生宿舍3号楼302室', '希望周末交易', 5999.00),
(3, 5, 1, '图书馆门口', '', 45.00),
(4, 7, 3, '快递点自提', '尽快发货', 450.00);

INSERT INTO favorites (user_id, item_id) VALUES
(2, 1),
(2, 6),
(3, 2),
(4, 1),
(5, 9),
(6, 10);

INSERT INTO chats (sender_id, receiver_id, content) VALUES
(1, 2, '您好，请问手机还在吗？'),
(2, 1, '在的，请问您感兴趣吗？'),
(1, 2, '是的，能便宜点吗？'),
(3, 4, '羽毛球拍还卖吗？'),
(4, 3, '卖的，周末可以看货');