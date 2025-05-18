CREATE SEQUENCE IF NOT EXISTS order_id_seq START WITH 10000000;
CREATE SEQUENCE IF NOT EXISTS product_id_seq START WITH 10000000;
CREATE TABLE IF NOT EXISTS orders (
                                      id INTEGER PRIMARY KEY DEFAULT nextval('order_id_seq'),
                                      order_number VARCHAR(50) NOT NULL,
                                      status VARCHAR(20) NOT NULL,
                                      orderer VARCHAR(50) NOT NULL,
                                      sub_total_price_in_cents INTEGER NOT NULL,
                                      total_price_in_cents INTEGER NOT NULL,
                                      currency VARCHAR(3),
                                      order_lines JSONB NOT NULL,
                                      discounts JSONB NOT NULL,
                                      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                      processed_at TIMESTAMP,
                                      canceled_at TIMESTAMP
);

ALTER SEQUENCE order_id_seq RESTART WITH 10000000;
ALTER SEQUENCE product_id_seq RESTART WITH 10000000;
TRUNCATE TABLE "orders" CASCADE;


INSERT INTO orders (
    id, order_number, status, orderer, sub_total_price_in_cents, total_price_in_cents, currency, order_lines, discounts, created_at, updated_at, processed_at, canceled_at
) VALUES
      (nextval('order_id_seq'),'ORD-1001', 'PENDING', 'Alice', 1200, 1000, 'EUR', '[{"price_in_cents":1200,"drink":{"name":"Latte","price_in_cents":700},"toppings":[{"name":"Chocolate Syrup","price_in_cents":500}]}]', '[]', '2023-10-01 10:00:00', '2023-10-01 10:00:00', NULL, NULL),
      (nextval('order_id_seq'),'ORD-1002', 'COMPLETED', 'Bob', 1300, 975, 'EUR', '[{"price_in_cents":1300,"drink":{"name":"Espresso","price_in_cents":1300},"toppings":[]}]', '[]', '2023-10-02 09:30:00', '2023-10-02 09:30:00', '2023-10-02 10:00:00', NULL),
      (nextval('order_id_seq'),'ORD-1003', 'CANCELLED', 'Charlie', 930, 650, 'EUR', '[{"price_in_cents":930,"drink":{"name":"Americano","price_in_cents":930},"toppings":[]}]', '[]', '2023-10-03 15:45:00', '2023-10-03 15:45:00', NULL, NULL),
      (nextval('order_id_seq'),'ORD-1004', 'PENDING', 'Diana', 300, 300, 'EUR', '[{"price_in_cents":300,"drink":{"name":"Tea","price_in_cents":300},"toppings":[]}]', '[]', '2023-10-04 11:20:00', '2023-10-04 11:20:00', NULL, NULL),
      (nextval('order_id_seq'),'ORD-1005', 'PENDING', 'Eve', 1500, 1200, 'EUR', '[{"price_in_cents":1500,"drink":{"name":"Cappuccino","price_in_cents":1200},"toppings":[{"name":"Vanilla","price_in_cents":300}]}]', '[]', '2023-10-05 08:10:00', '2023-10-05 08:10:00', NULL, NULL),
      (nextval('order_id_seq'),'ORD-1006', 'COMPLETED', 'Frank', 2000, 1800, 'USD', '[{"price_in_cents":2000,"drink":{"name":"Mocha","price_in_cents":1800},"toppings":[{"name":"Caramel","price_in_cents":200}]}]', '[]', '2023-10-06 12:00:00', '2023-10-06 12:00:00', '2023-10-06 12:30:00', NULL),
      (nextval('order_id_seq'),'ORD-1007', 'PENDING', 'Grace', 800, 800, 'USD', '[{"price_in_cents":800,"drink":{"name":"Flat White","price_in_cents":800},"toppings":[]}]', '[]', '2023-10-07 14:00:00', '2023-10-07 14:00:00', NULL, NULL),
      (nextval('order_id_seq'),'ORD-1008', 'CANCELLED', 'Heidi', 1100, 900, 'EUR', '[{"price_in_cents":1100,"drink":{"name":"Macchiato","price_in_cents":900},"toppings":[{"name":"Hazelnut","price_in_cents":200}]}]', '[]', '2023-10-08 16:00:00', '2023-10-08 16:00:00', NULL, '2023-10-08 16:30:00'),
      (nextval('order_id_seq'),'ORD-1009', 'COMPLETED', 'Ivan', 950, 950, 'USD', '[{"price_in_cents":950,"drink":{"name":"Black Coffee","price_in_cents":950},"toppings":[]}]', '[]', '2023-10-09 18:00:00', '2023-10-09 18:00:00', '2023-10-09 18:30:00', NULL),
      (nextval('order_id_seq'),'ORD-1010', 'PENDING', 'Judy', 1250, 1000, 'EUR', '[{"price_in_cents":1250,"drink":{"name":"Affogato","price_in_cents":1000},"toppings":[{"name":"Ice Cream","price_in_cents":250}]}]', '[]', '2023-10-10 20:00:00', '2023-10-10 20:00:00', NULL, NULL);

