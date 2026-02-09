INSERT INTO coupons (coupon_code, discount_type, discount_value, min_order_amount, valid_from, valid_until)
VALUES
  ('WELCOME10', 'PERCENT', 10, 1000, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
  ('SAVE1000', 'FIXED_AMOUNT', 1000, 3000, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
  ('EXPIRED10', 'PERCENT', 10, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59');
