INSERT INTO point_accounts (user_id, available_point_amount, created_at, updated_at)
VALUES
  ('user-1', 10000, '2026-01-01 00:00:00', '2026-01-01 00:00:00')
ON DUPLICATE KEY UPDATE
  available_point_amount = VALUES(available_point_amount),
  updated_at = VALUES(updated_at);

select 1;
