-- 先刪除已存在的表（如果存在）
DROP TABLE IF EXISTS customer_accounts CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- 創建 customers 表
CREATE TABLE customers (
                           customer_id VARCHAR(10) PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           email VARCHAR(100) NOT NULL,
                           phone VARCHAR(20) NOT NULL,
                           balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 創建 customer_accounts 表
CREATE TABLE customer_accounts (
                                   customer_id VARCHAR(10) PRIMARY KEY,
                                   name VARCHAR(100) NOT NULL,
                                   balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                                   version BIGINT NOT NULL DEFAULT 0,
                                   last_transaction_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- 插入測試客戶數據
INSERT INTO customers (customer_id, name, email, phone, balance, created_at, updated_at)
VALUES
    ('C001', '張三', 'zhang.san@email.com', '0912345678', 1000000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('C002', '李四', 'li.si@email.com', '0923456789', 500000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('C003', '王五', 'wang.wu@email.com', '0934567890', 750000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('C004', '趙六', 'zhao.liu@email.com', '0945678901', 250000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('C005', '孫七', 'sun.qi@email.com', '0956789012', 1500000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入客戶帳戶數據
INSERT INTO customer_accounts (customer_id, name, balance, version, last_transaction_time)
VALUES
    ('C001', '張三', 1000000.00, 0, CURRENT_TIMESTAMP),
    ('C002', '李四', 500000.00, 0, CURRENT_TIMESTAMP),
    ('C003', '王五', 750000.00, 0, CURRENT_TIMESTAMP),
    ('C004', '趙六', 250000.00, 0, CURRENT_TIMESTAMP),
    ('C005', '孫七', 1500000.00, 0, CURRENT_TIMESTAMP);

-- 創建索引（可選，但建議加上以提升查詢效能）
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone);
