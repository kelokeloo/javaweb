CREATE DATABASE IF NOT EXISTS mybatis_learning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mybatis_learning;
CREATE TABLE IF NOT EXISTS mybatis_product (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    brand VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    stock INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO mybatis_product (product_name, category, brand, price, status, stock)
SELECT * FROM (
    SELECT '华为 Mate 60', '手机', '华为', '6999.00', 'ON_SALE', 120 UNION ALL
    SELECT '小米 14', '手机', '小米', '4299.00', 'ON_SALE', 200 UNION ALL
    SELECT 'iPhone 15', '手机', '苹果', '5999.00', 'ON_SALE', 80 UNION ALL
    SELECT '华为智界 R', '手机', '华为', '7999.00', 'SOLD_OUT', 0 UNION ALL
    SELECT '联想拯救者 Y9000P', '笔记本', '联想', '8999.00', 'ON_SALE', 30 UNION ALL
    SELECT '华为 MateBook X Pro', '笔记本', '华为', '9999.00', 'ON_SALE', 15 UNION ALL
    SELECT '小米笔记本 Pro', '笔记本', '小米', '5999.00', 'OFF_SALE', 60 UNION ALL
    SELECT 'iPad Air', '平板', '苹果', '4799.00', 'ON_SALE', 50 UNION ALL
    SELECT '华为 MatePad Pro', '平板', '华为', '3999.00', 'ON_SALE', 90
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM mybatis_product);
