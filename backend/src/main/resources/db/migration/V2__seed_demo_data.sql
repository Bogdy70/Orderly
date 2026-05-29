INSERT INTO users (id, email, username, password_hash)
VALUES (1, 'demo@orderly.local', 'demo', 'mock-password-hash');

INSERT INTO spaces (id, owner_id, name, description, icon, color)
VALUES (1, 1, 'Demo Organization Space', 'Seed space with one checklist, one table and one diagram block.', 'layout-dashboard', '#e86d13');

INSERT INTO blocks (id, space_id, type, title, position)
VALUES
  (1, 1, 'checklist', 'Launch Checklist', 1),
  (2, 1, 'table', 'Action List', 2),
  (3, 1, 'diagram', 'Planning Diagram', 3);

INSERT INTO checklist_items (block_id, text, is_done, position)
VALUES
  (1, 'Confirm launch goals', true, 1),
  (1, 'Review launch timeline', false, 2),
  (1, 'Send kickoff update', false, 3);

INSERT INTO table_rows (block_id, title, status, priority, due_date, position)
VALUES
  (2, 'Finalize task board', 'done', 'medium', null, 1),
  (2, 'Review open blockers', 'pending', 'high', null, 2),
  (2, 'Prepare end-of-day recap', 'todo', 'low', null, 3);

INSERT INTO diagrams (id, block_id, viewport_x, viewport_y, zoom)
VALUES (1, 3, 0, 0, 1);

INSERT INTO diagram_nodes (id, diagram_id, type, label, x, y, width, height, style_json, data_json)
VALUES
  (1, 1, 'start', 'Collect Ideas', 80, 80, 160, 80, '{"color":"#e86d13"}', '{}'),
  (2, 1, 'process', 'Shape Plan', 320, 80, 160, 80, '{"color":"#2563eb"}', '{}');

INSERT INTO diagram_edges (diagram_id, source_node_id, target_node_id, label, type, style_json)
VALUES (1, 1, 2, 'next', 'arrow', '{}');

SELECT setval('users_id_seq', (SELECT max(id) FROM users));
SELECT setval('spaces_id_seq', (SELECT max(id) FROM spaces));
SELECT setval('blocks_id_seq', (SELECT max(id) FROM blocks));
SELECT setval('diagrams_id_seq', (SELECT max(id) FROM diagrams));
SELECT setval('diagram_nodes_id_seq', (SELECT max(id) FROM diagram_nodes));
