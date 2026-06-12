-- 添加支付截止时间字段
ALTER TABLE orders ADD COLUMN payment_deadline DATETIME;

-- 为现有订单设置支付截止时间（如果有的话）
UPDATE orders SET payment_deadline = DATE_ADD(created_at, INTERVAL 30 MINUTE) WHERE payment_deadline IS NULL;