CREATE TABLE IF NOT EXISTS transaction_logs (
                                                id BIGSERIAL PRIMARY KEY,
                                                transaction_id VARCHAR(255) NOT NULL,
                                                customer_id VARCHAR(255) NOT NULL,
                                                transaction_type VARCHAR(50) NOT NULL,
                                                amount DECIMAL(15,2) NOT NULL,
                                                status VARCHAR(50) NOT NULL,
                                                error_message TEXT,
                                                transaction_time TIMESTAMP NOT NULL,
                                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 分別創建索引
CREATE INDEX IF NOT EXISTS idx_transaction_id ON transaction_logs(transaction_id);
CREATE INDEX IF NOT EXISTS idx_customer_id ON transaction_logs(customer_id);
CREATE INDEX IF NOT EXISTS idx_transaction_time ON transaction_logs(transaction_time);
