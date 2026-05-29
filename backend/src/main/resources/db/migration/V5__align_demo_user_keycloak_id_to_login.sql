UPDATE users
SET keycloak_id = 'demo'
WHERE email = 'demo@orderly.local'
  AND username = 'demo'
  AND keycloak_id = '00000000-0000-0000-0000-000000000001';