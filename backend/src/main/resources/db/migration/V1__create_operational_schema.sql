CREATE TABLE customers (
    id VARCHAR(32) PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE
);

CREATE TABLE products (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0)
);

CREATE TABLE orders (
    id VARCHAR(32) PRIMARY KEY,
    customer_id VARCHAR(32) NOT NULL REFERENCES customers(id),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
    order_id VARCHAR(32) NOT NULL REFERENCES orders(id),
    product_id VARCHAR(32) NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    PRIMARY KEY (order_id, product_id)
);

CREATE TABLE deliveries (
    id VARCHAR(32) PRIMARY KEY,
    order_id VARCHAR(32) NOT NULL UNIQUE REFERENCES orders(id),
    status VARCHAR(32) NOT NULL,
    estimated_delivery_date DATE NOT NULL,
    delivered_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
