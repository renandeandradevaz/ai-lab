INSERT INTO customers (id, full_name, email) VALUES
    ('CUST-1001', 'Alice Johnson', 'alice@example.com'),
    ('CUST-1002', 'Bruno Martins', 'bruno@example.com');

INSERT INTO products (id, name, unit_price) VALUES
    ('PROD-1001', 'Wireless Keyboard', 79.90),
    ('PROD-1002', 'USB-C Hub', 129.90),
    ('PROD-1003', 'Laptop Stand', 159.90);

INSERT INTO orders (id, customer_id, status, created_at, total_amount) VALUES
    ('ORD-1001', 'CUST-1001', 'DELIVERED', '2026-08-20T10:00:00Z', 79.90),
    ('ORD-1002', 'CUST-1001', 'IN_TRANSIT', '2026-08-22T14:30:00Z', 289.80),
    ('ORD-1003', 'CUST-1002', 'DELAYED', '2026-08-18T09:15:00Z', 159.90),
    ('ORD-1004', 'CUST-1002', 'CANCELLED', '2026-08-15T16:45:00Z', 129.90);

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
    ('ORD-1001', 'PROD-1001', 1, 79.90),
    ('ORD-1002', 'PROD-1002', 1, 129.90),
    ('ORD-1002', 'PROD-1003', 1, 159.90),
    ('ORD-1003', 'PROD-1003', 1, 159.90),
    ('ORD-1004', 'PROD-1002', 1, 129.90);

INSERT INTO deliveries (id, order_id, status, estimated_delivery_date, delivered_at) VALUES
    ('DEL-1001', 'ORD-1001', 'DELIVERED', '2026-08-24', '2026-08-23T13:20:00Z'),
    ('DEL-1002', 'ORD-1002', 'IN_TRANSIT', '2026-08-30', NULL),
    ('DEL-1003', 'ORD-1003', 'DELAYED', '2026-08-25', NULL);
