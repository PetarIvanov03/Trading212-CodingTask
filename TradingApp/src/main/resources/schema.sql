CREATE TABLE IF NOT EXISTS balance (
    id INT PRIMARY KEY,
    amount DOUBLE
);

INSERT IGNORE INTO balance(id, amount) VALUES (1, 10000);

CREATE TABLE IF NOT EXISTS holdings (
    symbol VARCHAR(20) PRIMARY KEY,
    quantity DOUBLE NOT NULL,
    avg_price DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS trades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20),
    amount DOUBLE,
    price DOUBLE,
    profit_loss DOUBLE,
    balance_after DOUBLE,
    type VARCHAR(10),
    timestamp DATETIME
);
