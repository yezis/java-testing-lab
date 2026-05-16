CREATE TABLE orders (
    product_id VARCHAR(64) PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL
);
