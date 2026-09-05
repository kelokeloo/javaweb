USE mybatis_learning;

CREATE TABLE IF NOT EXISTS mybatis_customer (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mybatis_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer
        FOREIGN KEY (customer_id) REFERENCES mybatis_customer(customer_id)
);

INSERT INTO mybatis_customer (name, email, phone)
SELECT '张三', 'zhangsan@example.com', '13800000001'
WHERE NOT EXISTS (
    SELECT 1 FROM mybatis_customer WHERE email = 'zhangsan@example.com'
);

INSERT INTO mybatis_customer (name, email, phone)
SELECT '李四', 'lisi@example.com', '13800000002'
WHERE NOT EXISTS (
    SELECT 1 FROM mybatis_customer WHERE email = 'lisi@example.com'
);

INSERT INTO mybatis_order (order_no, customer_id, total_amount, status)
SELECT 'ORD-20260905-001', customer_id, 299.00, 'PAID'
FROM mybatis_customer
WHERE email = 'zhangsan@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM mybatis_order WHERE order_no = 'ORD-20260905-001'
  );

INSERT INTO mybatis_order (order_no, customer_id, total_amount, status)
SELECT 'ORD-20260905-002', customer_id, 128.50, 'PENDING'
FROM mybatis_customer
WHERE email = 'zhangsan@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM mybatis_order WHERE order_no = 'ORD-20260905-002'
  );

INSERT INTO mybatis_order (order_no, customer_id, total_amount, status)
SELECT 'ORD-20260905-003', customer_id, 88.00, 'SHIPPED'
FROM mybatis_customer
WHERE email = 'lisi@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM mybatis_order WHERE order_no = 'ORD-20260905-003'
  );
