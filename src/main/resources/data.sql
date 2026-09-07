TRUNCATE TABLE items RESTART IDENTITY CASCADE;
TRUNCATE TABLE boxes RESTART IDENTITY CASCADE;

INSERT INTO boxes (txref, weight_limit, battery_capacity, state) VALUES
('BOX-0001', 500.00, 80, 'IDLE'),
('BOX-0002', 300.00, 15, 'IDLE'),
('BOX-0003', 450.00, 60, 'LOADED'),
('BOX-0004', 200.00, 90, 'IDLE'),
('BOX-0005', 500.00, 25, 'IDLE');

INSERT INTO items (name, weight, code, box_id) VALUES
('First_Aid-Kit', 150.00, 'MED_001', 3),
('Water_Bottle', 250.00, 'WTR_002', 3);