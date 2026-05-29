CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255),
  auth_provider_id VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE spaces (
  id BIGSERIAL PRIMARY KEY,
  owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(160) NOT NULL,
  description TEXT,
  icon VARCHAR(80),
  color VARCHAR(40),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE blocks (
  id BIGSERIAL PRIMARY KEY,
  space_id BIGINT NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
  type VARCHAR(30) NOT NULL CHECK (type IN ('checklist', 'table', 'diagram')),
  title VARCHAR(160) NOT NULL,
  position INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE checklist_items (
  id BIGSERIAL PRIMARY KEY,
  block_id BIGINT NOT NULL REFERENCES blocks(id) ON DELETE CASCADE,
  text TEXT NOT NULL,
  is_done BOOLEAN NOT NULL DEFAULT false,
  position INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE table_rows (
  id BIGSERIAL PRIMARY KEY,
  block_id BIGINT NOT NULL REFERENCES blocks(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'todo' CHECK (status IN ('todo', 'pending', 'done')),
  priority VARCHAR(30),
  due_date DATE,
  position INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE diagrams (
  id BIGSERIAL PRIMARY KEY,
  block_id BIGINT NOT NULL UNIQUE REFERENCES blocks(id) ON DELETE CASCADE,
  viewport_x DOUBLE PRECISION NOT NULL DEFAULT 0,
  viewport_y DOUBLE PRECISION NOT NULL DEFAULT 0,
  zoom DOUBLE PRECISION NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE diagram_nodes (
  id BIGSERIAL PRIMARY KEY,
  diagram_id BIGINT NOT NULL REFERENCES diagrams(id) ON DELETE CASCADE,
  type VARCHAR(60) NOT NULL DEFAULT 'default',
  label TEXT NOT NULL,
  x DOUBLE PRECISION NOT NULL,
  y DOUBLE PRECISION NOT NULL,
  width DOUBLE PRECISION NOT NULL,
  height DOUBLE PRECISION NOT NULL,
  style_json JSONB,
  data_json JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE diagram_edges (
  id BIGSERIAL PRIMARY KEY,
  diagram_id BIGINT NOT NULL REFERENCES diagrams(id) ON DELETE CASCADE,
  source_node_id BIGINT NOT NULL REFERENCES diagram_nodes(id) ON DELETE CASCADE,
  target_node_id BIGINT NOT NULL REFERENCES diagram_nodes(id) ON DELETE CASCADE,
  label TEXT,
  type VARCHAR(60) NOT NULL DEFAULT 'arrow',
  style_json JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (source_node_id <> target_node_id)
);

CREATE INDEX idx_spaces_owner_id ON spaces(owner_id);
CREATE INDEX idx_blocks_space_id ON blocks(space_id);
CREATE INDEX idx_checklist_items_block_id ON checklist_items(block_id);
CREATE INDEX idx_table_rows_block_id ON table_rows(block_id);
CREATE INDEX idx_diagram_nodes_diagram_id ON diagram_nodes(diagram_id);
CREATE INDEX idx_diagram_edges_diagram_id ON diagram_edges(diagram_id);
