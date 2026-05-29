CREATE UNIQUE INDEX idx_users_auth_provider_id_unique
  ON users(auth_provider_id)
  WHERE auth_provider_id IS NOT NULL;
