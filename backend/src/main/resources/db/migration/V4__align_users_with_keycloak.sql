DROP INDEX IF EXISTS idx_users_auth_provider_id_unique;

ALTER TABLE users
  RENAME COLUMN auth_provider_id TO keycloak_id;

UPDATE users
SET keycloak_id = '00000000-0000-0000-0000-000000000001'
WHERE email = 'demo@orderly.local' AND keycloak_id IS NULL;

ALTER TABLE users
  ALTER COLUMN keycloak_id SET NOT NULL;

ALTER TABLE users
  DROP COLUMN IF EXISTS password_hash;

ALTER TABLE users
  ADD CONSTRAINT uk_users_keycloak_id UNIQUE (keycloak_id);
