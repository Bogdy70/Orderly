import { useEffect, useState } from "react";
import {
  clearToken,
  convertBlock,
  createBlock,
  createChecklistItem,
  createDiagram,
  createDiagramEdge,
  createDiagramNode,
  createSpace,
  createTableRow,
  decodeToken,
  deleteBlock,
  deleteChecklistItem,
  deleteDiagramEdge,
  deleteDiagramNode,
  deleteSpace,
  deleteTableRow,
  getCurrentUser,
  getSpaceFull,
  isAuthenticated,
  listSpaces,
  login,
  registerAccount,
  updateBlock,
  updateChecklistItem,
  updateDiagramEdge,
  updateDiagramNode,
  updateTableRow
} from "./services/api.js";

const EMPTY_SPACE = { name: "", description: "", icon: "folder", color: "#2563eb" };
const EMPTY_BLOCK = { type: "checklist", title: "", position: 1 };
const BLOCK_TYPES = ["checklist", "table", "diagram"];
const SHAPES = ["task", "note", "milestone", "process", "document", "database", "input", "output", "terminator", "card", "capsule", "stamp", "flag", "wave", "portal", "burst"];
const EDGE_TYPES = ["arrow", "curved", "elbow", "dashed", "dotted", "dash-dot", "bold", "soft", "double", "plain"];

function App() {
  const [route, setRoute] = useState(getRoute());

  useEffect(() => {
    const onPop = () => setRoute(getRoute());
    window.addEventListener("popstate", onPop);
    return () => window.removeEventListener("popstate", onPop);
  }, []);

  function navigate(path) {
    window.history.pushState({}, "", path);
    setRoute(getRoute());
  }

  if (route.page === "login") return <LoginPage navigate={navigate} />;
  if (route.page === "dashboard") return <DashboardPage navigate={navigate} />;
  if (route.page === "space") return <SpacePage navigate={navigate} spaceId={route.spaceId} />;
  return <RegisterPage navigate={navigate} />;
}

function RegisterPage({ navigate }) {
  const [form, setForm] = useState({ email: "", username: "", password: "", confirmPassword: "" });
  const [fieldErrors, setFieldErrors] = useState({});
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    const payload = {
      email: form.email.trim().toLowerCase(),
      username: form.username.trim(),
      password: form.password
    };
    const errors = validateRegistration({ ...payload, confirmPassword: form.confirmPassword });
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      setMessage("Check the highlighted fields.");
      return;
    }

    setBusy(true);
    setMessage("");
    try {
      await registerAccount(payload);
      await login(payload.username, payload.password);
      await getCurrentUser();
      navigate("/dashboard.html");
    } catch (error) {
      if (error.data?.fields) setFieldErrors(error.data.fields);
      setMessage(error.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-copy">
        <span className="eyebrow">Orderly</span>
        <h1>Spaces for plans, lists, tables, and visual diagrams.</h1>
        <p>Create a space for every project, then add movable blocks that fit the work: checklists for steps, tables for structured tasks, and diagrams for mapping ideas.</p>
        <div className="feature-strip">
          <span>Multiple spaces</span>
          <span>Block canvas</span>
          <span>Live diagrams</span>
        </div>
      </section>
      <section className="auth-card">
        <h2>Create your account</h2>
        <form className="form-stack" onSubmit={submit} noValidate>
          <Field label="Email" value={form.email} onChange={email => setForm({ ...form, email })} type="email" error={fieldErrors.email} autoComplete="email" />
          <Field label="Username" value={form.username} onChange={username => setForm({ ...form, username })} error={fieldErrors.username} autoComplete="username" />
          <Field label="Password" value={form.password} onChange={password => setForm({ ...form, password })} type="password" error={fieldErrors.password} autoComplete="new-password" />
          <Field label="Confirm password" value={form.confirmPassword} onChange={confirmPassword => setForm({ ...form, confirmPassword })} type="password" error={fieldErrors.confirmPassword} autoComplete="new-password" />
          <button className="button primary" disabled={busy}>{busy ? "Creating account..." : "Register"}</button>
        </form>
        {message && <p className="form-message">{message}</p>}
        <button className="link-button" onClick={() => navigate("/login.html")}>Already have an account? Log in</button>
      </section>
    </main>
  );
}

function LoginPage({ navigate }) {
  const [form, setForm] = useState({ username: "", password: "" });
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
      setMessage("");
    try {
      await login(form.username, form.password);
      await getCurrentUser();
      navigate("/dashboard.html");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-layout compact">
      <section className="auth-card">
        <h1>Log in</h1>
        <form className="form-stack" onSubmit={submit}>
          <Field label="Email or username" value={form.username} onChange={username => setForm({ ...form, username })} />
          <Field label="Password" value={form.password} onChange={password => setForm({ ...form, password })} type="password" />
          <button className="button primary" disabled={busy}>{busy ? "Signing in..." : "Log in"}</button>
        </form>
        {message && <p className="form-message">{message}</p>}
        <button className="link-button" onClick={() => navigate("/")}>Need an account? Register</button>
      </section>
    </main>
  );
}

function DashboardPage({ navigate }) {
  const [spaces, setSpaces] = useState([]);
  const [user, setUser] = useState(null);
  const [form, setForm] = useState(EMPTY_SPACE);
  const [query, setQuery] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    requireAuth(navigate);
    refresh();
  }, []);

  async function refresh() {
    try {
      setUser(await getCurrentUser());
      setSpaces(await listSpaces());
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function submit(event) {
    event.preventDefault();
    const payload = { ...form, name: form.name.trim(), description: form.description.trim() };
    if (!payload.name) return;
    await createSpace(payload);
    setForm(EMPTY_SPACE);
    refresh();
  }

  async function removeSpace(space) {
    if (!confirm(`Delete ${space.name}?`)) return;
    await deleteSpace(space.id);
    refresh();
  }

  const visibleSpaces = spaces.filter(space => `${space.name} ${space.description}`.toLowerCase().includes(query.toLowerCase()));

  return (
    <AppShell user={user} navigate={navigate}>
      <section className="page-heading">
        <div>
          <h1>Your spaces</h1>
          <p>Open a workspace or create a new one for a project, routine, or plan.</p>
        </div>
        <input placeholder="Search spaces" value={query} onChange={event => setQuery(event.target.value)} />
      </section>

      <section className="panel">
        <h2>Create space</h2>
        <form className="space-form" onSubmit={submit}>
          <Field label="Name" value={form.name} onChange={name => setForm({ ...form, name })} />
          <Field label="Description" value={form.description} onChange={description => setForm({ ...form, description })} />
          <Field label="Icon" value={form.icon} onChange={icon => setForm({ ...form, icon })} />
          <label>
            <span>Color</span>
            <input type="color" value={form.color} onChange={event => setForm({ ...form, color: event.target.value })} />
          </label>
          <button className="button primary">Create</button>
        </form>
      </section>

      {message && <Alert>{message}</Alert>}
      <section className="space-grid">
        {visibleSpaces.map(space => (
          <article className="space-card" key={space.id} style={{ borderTopColor: space.color || "#2563eb" }}>
            <div className="space-icon">{space.icon || "space"}</div>
            <h2>{space.name}</h2>
            <p>{space.description || "No description yet."}</p>
            <div className="card-actions">
              <button className="button primary" onClick={() => navigate(`/project.html?id=${space.id}`)}>Open</button>
              <button className="button danger" onClick={() => removeSpace(space)}>Delete</button>
            </div>
          </article>
        ))}
      </section>
    </AppShell>
  );
}

function SpacePage({ navigate, spaceId }) {
  const [space, setSpace] = useState(null);
  const [blocks, setBlocks] = useState([]);
  const [blockForm, setBlockForm] = useState(EMPTY_BLOCK);
  const [draggedBlockId, setDraggedBlockId] = useState(null);
  const [dropTargetId, setDropTargetId] = useState(null);

  useEffect(() => {
    requireAuth(navigate);
    refresh();
  }, [spaceId]);

  async function refresh() {
    const data = await getSpaceFull(spaceId);
    setSpace(data.space);
    setBlocks(data.blocks || []);
  }

  async function addBlock(event) {
    event.preventDefault();
    const title = blockForm.title.trim() || titleFor(blockForm.type);
    const position = blocks.length + 1;
    const block = await createBlock(spaceId, { ...blockForm, title, position });
    if (block.type === "diagram") await createDiagram(block.id);
    setBlockForm(EMPTY_BLOCK);
    refresh();
  }

  const orderedBlocks = [...blocks].sort((left, right) =>
    (left.block.position ?? 0) - (right.block.position ?? 0) || left.block.id - right.block.id
  );

  function startReorder(event, blockId) {
    setDraggedBlockId(blockId);
    setDropTargetId(blockId);
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData("text/plain", String(blockId));
  }

  function allowDrop(event, blockId) {
    event.preventDefault();
    if (draggedBlockId && draggedBlockId !== blockId) {
      setDropTargetId(blockId);
    }
  }

  async function reorderBlocks(sourceId, targetId) {
    setDraggedBlockId(null);
    setDropTargetId(null);
    if (!sourceId || !targetId || sourceId === targetId) return;

    const sourceIndex = orderedBlocks.findIndex(item => item.block.id === sourceId);
    const targetIndex = orderedBlocks.findIndex(item => item.block.id === targetId);
    if (sourceIndex < 0 || targetIndex < 0) return;

    const next = [...orderedBlocks];
    const [moved] = next.splice(sourceIndex, 1);
    next.splice(targetIndex, 0, moved);
    const positioned = next.map((item, index) => ({
      ...item,
      block: { ...item.block, position: index + 1 }
    }));

    setBlocks(positioned);
    await Promise.all(positioned.map(item => updateBlock(item.block.id, { position: item.block.position })));
    refresh();
  }

  async function convertAndRefresh(block, targetType) {
    await convertBlock(block.id, targetType);
    await refresh();
  }

  return (
    <AppShell navigate={navigate}>
      <section className="space-title">
        <button className="button secondary" onClick={() => navigate("/dashboard.html")}>Back</button>
        <div>
          <h1>{space?.name || "Space"}</h1>
          <p>{space?.description}</p>
        </div>
      </section>

      <section className="panel">
        <form className="block-form" onSubmit={addBlock}>
          <label>
            <span>Block type</span>
            <select value={blockForm.type} onChange={event => setBlockForm({ ...blockForm, type: event.target.value })}>
              <option value="checklist">Checklist</option>
              <option value="table">Table</option>
              <option value="diagram">Diagram</option>
            </select>
          </label>
          <Field label="Title" value={blockForm.title} onChange={title => setBlockForm({ ...blockForm, title })} />
          <button className="button primary">Add block</button>
        </form>
      </section>

      <section
        className="block-grid"
        onDragOver={event => event.preventDefault()}
        onDrop={event => {
          if (event.target !== event.currentTarget) return;
          event.preventDefault();
          const sourceId = Number(event.dataTransfer.getData("text/plain")) || draggedBlockId;
          const lastBlock = orderedBlocks[orderedBlocks.length - 1];
          if (lastBlock) reorderBlocks(sourceId, lastBlock.block.id);
        }}
      >
        {orderedBlocks.map(item => (
          <article
            className={`block-card block-${item.block.type} ${draggedBlockId === item.block.id ? "dragging" : ""} ${dropTargetId === item.block.id && draggedBlockId !== item.block.id ? "drop-target" : ""}`}
            key={item.block.id}
            onDragOver={event => allowDrop(event, item.block.id)}
            onDrop={event => {
              event.preventDefault();
              const sourceId = Number(event.dataTransfer.getData("text/plain")) || draggedBlockId;
              reorderBlocks(sourceId, item.block.id);
            }}
          >
            <header
              className="block-header"
              draggable
              onDragStart={event => startReorder(event, item.block.id)}
              onDragEnd={() => {
                setDraggedBlockId(null);
                setDropTargetId(null);
              }}
            >
              <div>
                <span>{item.block.type}</span>
                <h2>{item.block.title}</h2>
              </div>
              <div className="block-actions" draggable="false" onDragStart={event => event.stopPropagation()}>
                <ConversionMenu block={item.block} onConvert={targetType => convertAndRefresh(item.block, targetType)} />
                <button className="button danger small" onClick={() => deleteBlock(item.block.id).then(refresh)}>Delete</button>
              </div>
            </header>
            {item.block.type === "checklist" && <ChecklistBlock block={item.block} items={item.content || []} refresh={refresh} />}
            {item.block.type === "table" && <TableBlock block={item.block} rows={item.content || []} refresh={refresh} />}
            {item.block.type === "diagram" && <DiagramBlock block={item.block} content={item.content} refresh={refresh} />}
          </article>
        ))}
      </section>
    </AppShell>
  );
}

function ConversionMenu({ block, onConvert }) {
  const options = BLOCK_TYPES.filter(type => type !== block.type);

  return (
    <details className="convert-dropdown">
      <summary>Convert</summary>
      <div className="convert-menu">
        {options.map(type => (
          <button type="button" key={type} onClick={() => onConvert(type)}>
            <span>Convert to</span>
            {titleFor(type).replace("New ", "")}
          </button>
        ))}
      </div>
    </details>
  );
}

function ChecklistBlock({ block, items, refresh }) {
  const [text, setText] = useState("");
  const [editingItem, setEditingItem] = useState(null);

  function editItem(item) {
    setEditingItem(item);
    setText(item.text);
  }

  function clearEdit() {
    setEditingItem(null);
    setText("");
  }

  async function submit(event) {
    event.preventDefault();
    const nextText = text.trim();
    if (!nextText) return;
    if (editingItem) {
      await updateChecklistItem(editingItem.id, { text: nextText });
    } else {
      await createChecklistItem(block.id, { text: nextText, done: false, position: items.length + 1 });
    }
    clearEdit();
    refresh();
  }

  return (
    <div className="block-body">
      <form className="inline-form editable-form" onSubmit={submit}>
        <input placeholder="Checklist item" value={text} onChange={event => setText(event.target.value)} />
        <button className="button primary small">{editingItem ? "Save" : "Add"}</button>
        {editingItem && <button className="button secondary small icon-only" type="button" aria-label="Cancel edit" onClick={clearEdit}>X</button>}
      </form>
      <ul className="checklist">
        {items.map(item => (
          <li key={item.id}>
            <label>
              <input type="checkbox" checked={item.done} onChange={() => updateChecklistItem(item.id, { done: !item.done }).then(refresh)} />
              <span className={item.done ? "done" : ""}>{item.text}</span>
            </label>
            <div className="row-actions">
              <button className="button secondary small" type="button" onClick={() => editItem(item)}>Edit</button>
              <button className="text-danger" type="button" onClick={() => deleteChecklistItem(item.id).then(refresh)}>Delete</button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

function TableBlock({ block, rows, refresh }) {
  const [form, setForm] = useState({ title: "", status: "todo", priority: "low", dueDate: "" });
  const [editingRow, setEditingRow] = useState(null);

  function editRow(row) {
    setEditingRow(row);
    setForm({
      title: row.title || "",
      status: row.status || "todo",
      priority: row.priority || "low",
      dueDate: row.dueDate || ""
    });
  }

  function clearEdit() {
    setEditingRow(null);
    setForm({ title: "", status: "todo", priority: "low", dueDate: "" });
  }

  async function submit(event) {
    event.preventDefault();
    if (!form.title.trim()) return;
    const payload = { ...form, title: form.title.trim(), priority: form.priority || "low", dueDate: form.dueDate || null };
    if (editingRow) {
      await updateTableRow(editingRow.id, payload);
    } else {
      await createTableRow(block.id, { ...payload, position: rows.length + 1 });
    }
    clearEdit();
    refresh();
  }
  return (
    <div className="block-body">
      <form className="table-form" onSubmit={submit}>
        <input placeholder="Row title" value={form.title} onChange={event => setForm({ ...form, title: event.target.value })} />
        <select value={form.status} onChange={event => setForm({ ...form, status: event.target.value })}>
          <option value="todo">todo</option>
          <option value="pending">pending</option>
          <option value="done">done</option>
        </select>
        <select value={form.priority || "low"} onChange={event => setForm({ ...form, priority: event.target.value })}>
          <option value="low">low</option>
          <option value="medium">medium</option>
          <option value="high">high</option>
        </select>
        <input type="date" value={form.dueDate || ""} onChange={event => setForm({ ...form, dueDate: event.target.value })} />
        <button className="button primary small">{editingRow ? "Save" : "Add"}</button>
        {editingRow && <button className="button secondary small icon-only" type="button" aria-label="Cancel edit" onClick={clearEdit}>X</button>}
      </form>
      <div className="rows">
        {rows.length > 0 && (
          <div className="data-row table-head">
            <span>Task name</span>
            <span>Status</span>
            <span>Priority</span>
            <span>Due date</span>
            <span>Actions</span>
          </div>
        )}
        {rows.map(row => (
          <div className="data-row" key={row.id}>
            <span className="row-title">{row.title}</span>
            <span><StatusBadge status={row.status} /></span>
            <span><PriorityBadge priority={row.priority || "low"} /></span>
            <span>{row.dueDate || "no date"}</span>
            <div className="row-actions">
              <button className="button secondary small" type="button" onClick={() => editRow(row)}>Edit</button>
              <button className="text-danger" type="button" onClick={() => deleteTableRow(row.id).then(refresh)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function StatusBadge({ status }) {
  const value = status || "todo";
  return <span className={`badge status-${value}`}>{value}</span>;
}

function PriorityBadge({ priority }) {
  const value = priority || "low";
  return <span className={`badge priority-${value}`}>{value}</span>;
}

function DiagramBlock({ block, content, refresh }) {
  const [selectedNode, setSelectedNode] = useState(null);
  const [connectionNodes, setConnectionNodes] = useState([]);
  const [selectedEdges, setSelectedEdges] = useState(new Set());
  const [drag, setDrag] = useState(null);
  const [draft, setDraft] = useState({ label: "", type: "task", color: "#2563eb" });
  const [edgeDraft, setEdgeDraft] = useState({ label: "", type: "arrow" });
  const diagram = content?.diagram;
  const nodes = content?.nodes || [];
  const edges = content?.edges || [];
  const selectedEdgeId = [...selectedEdges][0] || null;
  const selectedEdge = edges.find(edge => edge.id === selectedEdgeId);

  useEffect(() => {
    function onKeyDown(event) {
      if (isTextInput(event.target)) return;
      if (event.key === "Backspace" && selectedEdges.size) {
        event.preventDefault();
        deleteSelectedEdges();
      }
      if (event.key === "Enter" && connectionNodes.length === 2 && diagram) {
        event.preventDefault();
        connectSelectedNodes();
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [selectedEdges, connectionNodes, diagram, edgeDraft, edges]);

  useEffect(() => {
    if (!selectedNode) return;
    const node = nodes.find(item => item.id === selectedNode);
    if (!node) return;
    const style = parseJson(node.styleJson);
    setDraft({ label: node.label, type: node.type || "task", color: style.color || "#2563eb" });
  }, [selectedNode]);

  function selectNode(nodeId) {
    setSelectedNode(nodeId);
    setSelectedEdges(new Set());
    setConnectionNodes(current => {
      if (current.includes(nodeId)) return current;
      return [...current, nodeId].slice(-2);
    });
  }

  function clearNodeForm() {
    setSelectedNode(null);
    setSelectedEdges(new Set());
    setConnectionNodes([]);
    setDraft({ label: "", type: "task", color: "#2563eb" });
    setEdgeDraft({ label: "", type: "arrow" });
  }

  async function ensureDiagram() {
    if (diagram) return diagram;
    await createDiagram(block.id);
    await refresh();
    return null;
  }

  async function submitNode() {
    if (selectedNode) {
      await updateDiagramNode(selectedNode, {
        label: draft.label || "New node",
        type: draft.type,
        styleJson: JSON.stringify({ color: draft.color })
      });
      refresh();
      return;
    }

    const currentDiagram = await ensureDiagram();
    if (!currentDiagram) return;
    await createDiagramNode(currentDiagram.id, {
      type: draft.type,
      label: draft.label || "New node",
      x: 60 + nodes.length * 28,
      y: 70 + nodes.length * 28,
      width: 150,
      height: 76,
      styleJson: JSON.stringify({ color: draft.color }),
      dataJson: "{}"
    });
    setDraft({ label: "", type: "task", color: "#2563eb" });
    refresh();
  }

  async function connectSelectedNodes() {
    if (!diagram || connectionNodes.length !== 2 || connectionNodes[0] === connectionNodes[1]) return;
    await createDiagramEdge(diagram.id, {
      sourceNodeId: connectionNodes[0],
      targetNodeId: connectionNodes[1],
      type: edgeDraft.type,
      label: edgeDraft.label.trim(),
      styleJson: "{}"
    });
    setConnectionNodes([]);
    setEdgeDraft({ label: "", type: "arrow" });
    refresh();
  }

  async function saveSelectedEdge() {
    if (!selectedEdgeId) return;
    await updateDiagramEdge(selectedEdgeId, {
      label: edgeDraft.label.trim(),
      type: edgeDraft.type
    });
    refresh();
  }

  async function removeNode() {
    if (!selectedNode) return;
    await deleteDiagramNode(selectedNode);
    clearNodeForm();
    setConnectionNodes(current => current.filter(id => id !== selectedNode));
    refresh();
  }

  async function deleteSelectedEdges() {
    await Promise.all([...selectedEdges].map(id => deleteDiagramEdge(id)));
    setSelectedEdges(new Set());
    refresh();
  }

  async function endDrag() {
    if (!drag) return;
    await updateDiagramNode(drag.id, { x: drag.x, y: drag.y });
    setDrag(null);
    refresh();
  }

  const renderedNodes = nodes.map(node => drag?.id === node.id ? { ...node, x: drag.x, y: drag.y } : node);

  return (
    <div className="diagram-shell">
      <div className="diagram-tools">
        <input placeholder="Node label" value={draft.label} onChange={event => setDraft({ ...draft, label: event.target.value })} />
        <select value={draft.type} onChange={event => setDraft({ ...draft, type: event.target.value })}>
          {SHAPES.map(shape => <option key={shape}>{shape}</option>)}
        </select>
        <input type="color" value={draft.color} onChange={event => setDraft({ ...draft, color: event.target.value })} />
        <button className="button primary small" onClick={submitNode}>{selectedNode ? "Save shape" : "Add shape"}</button>
        <button className="button secondary small" onClick={clearNodeForm}>Clear</button>
        <button className="button secondary small" disabled={connectionNodes.length !== 2 || !diagram} onClick={connectSelectedNodes}>Connect</button>
        <button className="button danger small" disabled={!selectedNode} onClick={removeNode}>Delete shape</button>
        <button className="button danger small" disabled={!selectedEdges.size} onClick={deleteSelectedEdges}>Delete arrow</button>
        <input className="edge-label-input" placeholder={selectedEdge ? "Arrow label" : "New arrow label"} value={edgeDraft.label} onChange={event => setEdgeDraft({ ...edgeDraft, label: event.target.value })} />
        <select className="edge-type-select" value={edgeDraft.type} onChange={event => setEdgeDraft({ ...edgeDraft, type: event.target.value })}>
          {EDGE_TYPES.map(type => <option key={type} value={type}>{type}</option>)}
        </select>
        <button className="button secondary small" disabled={!selectedEdge} onClick={saveSelectedEdge}>Save arrow</button>
      </div>
      <div
        className="diagram-canvas"
        onClick={clearNodeForm}
        onPointerMove={event => {
          if (!drag) return;
          const rect = event.currentTarget.getBoundingClientRect();
          setDrag({ ...drag, x: event.clientX - rect.left - drag.offsetX, y: event.clientY - rect.top - drag.offsetY });
        }}
        onPointerUp={endDrag}
      >
        <svg className="edge-layer">
          <defs>
            <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto">
              <path d="M0,0 L0,6 L9,3 z" fill="#5d4b40" />
            </marker>
            <marker id="arrow-start" markerWidth="10" markerHeight="10" refX="1" refY="3" orient="auto">
              <path d="M9,0 L9,6 L0,3 z" fill="#5d4b40" />
            </marker>
          </defs>
          {edges.map(edge => {
            const source = renderedNodes.find(node => node.id === edge.sourceNodeId);
            const target = renderedNodes.find(node => node.id === edge.targetNodeId);
            if (!source || !target) return null;
            const selected = selectedEdges.has(edge.id);
            const type = edge.type || "arrow";
            const geometry = edgeGeometry(source, target, type);
            const label = edge.label?.trim();
            return (
              <g
                key={edge.id}
                className="edge-group"
                onClick={event => {
                  event.stopPropagation();
                  setSelectedEdges(new Set([edge.id]));
                  setSelectedNode(null);
                  setConnectionNodes([]);
                  setDraft({ label: "", type: "task", color: "#2563eb" });
                  setEdgeDraft({ label: edge.label || "", type });
                }}
              >
                <path
                  d={geometry.path}
                  className={`edge edge-${type} ${selected ? "selected" : ""}`}
                  markerStart={type === "double" ? "url(#arrow-start)" : undefined}
                  markerEnd={type === "plain" ? undefined : "url(#arrow)"}
                />
                {label && (
                  <text className={`edge-label ${selected ? "selected" : ""}`} x={geometry.label.x} y={geometry.label.y - 8}>
                    {label}
                  </text>
                )}
              </g>
            );
          })}
        </svg>
        {renderedNodes.map(node => {
          const style = parseJson(node.styleJson);
          return (
            <button
              key={node.id}
              className={`diagram-node ${node.type} ${selectedNode === node.id ? "selected" : ""} ${connectionNodes[0] === node.id ? "connect-start" : ""} ${connectionNodes[1] === node.id ? "connect-target" : ""}`}
              style={{ left: node.x, top: node.y, width: node.width, height: node.height, borderColor: style.color || "#2563eb", "--node-color": style.color || "#2563eb" }}
              onClick={event => {
                event.stopPropagation();
                selectNode(node.id);
              }}
              onPointerDown={event => {
                event.stopPropagation();
                setDrag({ id: node.id, x: node.x, y: node.y, offsetX: event.nativeEvent.offsetX, offsetY: event.nativeEvent.offsetY });
              }}
            >
              {node.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}

function AppShell({ user, navigate, children }) {
  const claims = decodeToken();
  const name = user?.username || claims.preferred_username || "User";
  return (
    <>
      <header className="topbar">
        <button className="brand" onClick={() => navigate("/dashboard.html")}>Orderly</button>
        <nav>
          <span>{name}</span>
          <button className="button secondary small" onClick={() => navigate("/dashboard.html")}>Dashboard</button>
          <button className="button danger small" onClick={() => { clearToken(); navigate("/login.html"); }}>Log out</button>
        </nav>
      </header>
      <main className="app-page">{children}</main>
    </>
  );
}

function Field({ label, value, onChange, type = "text", error, ...inputProps }) {
  return (
    <label>
      <span>{label}</span>
      <input
        className={error ? "input-error" : ""}
        type={type}
        value={value}
        onChange={event => onChange(event.target.value)}
        required
        {...inputProps}
      />
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}

function Alert({ children }) {
  return <div className="alert">{children}</div>;
}

function requireAuth(navigate) {
  if (!isAuthenticated()) navigate("/login.html");
}

function getRoute() {
  const path = window.location.pathname.toLowerCase();
  const params = new URLSearchParams(window.location.search);
  if (path.endsWith("/login.html")) return { page: "login" };
  if (path.endsWith("/dashboard.html")) return { page: "dashboard" };
  if (path.endsWith("/project.html")) return { page: "space", spaceId: params.get("id") };
  return { page: "register" };
}

function titleFor(type) {
  if (type === "table") return "New Table";
  if (type === "diagram") return "New Diagram";
  return "New Checklist";
}

function validateRegistration(form) {
  const errors = {};
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const usernamePattern = /^[a-zA-Z0-9._-]+$/;

  if (!form.email) {
    errors.email = "Email is required.";
  } else if (!emailPattern.test(form.email)) {
    errors.email = "Enter a valid email address.";
  } else if (form.email.length > 255) {
    errors.email = "Email must be 255 characters or fewer.";
  }

  if (!form.username) {
    errors.username = "Username is required.";
  } else if (form.username.length < 3) {
    errors.username = "Username must be at least 3 characters.";
  } else if (form.username.length > 100) {
    errors.username = "Username must be 100 characters or fewer.";
  } else if (!usernamePattern.test(form.username)) {
    errors.username = "Use letters, numbers, dots, underscores, or hyphens.";
  }

  if (!form.password) {
    errors.password = "Password is required.";
  } else if (form.password.length < 8) {
    errors.password = "Password must be at least 8 characters.";
  } else if (form.password.length > 100) {
    errors.password = "Password must be 100 characters or fewer.";
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = "Confirm your password.";
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = "Passwords do not match.";
  }

  return errors;
}

function isTextInput(target) {
  if (!target) return false;
  const tag = target.tagName?.toLowerCase();
  return tag === "input" || tag === "select" || tag === "textarea" || target.isContentEditable;
}

function edgeGeometry(source, target, type) {
  const sourceCenter = centerOf(source);
  const targetCenter = centerOf(target);
  const start = pointOnRectEdge(source, targetCenter);
  const end = pointOnRectEdge(target, sourceCenter);

  if (type === "curved") {
    const dx = end.x - start.x;
    const dy = end.y - start.y;
    const distance = Math.max(Math.hypot(dx, dy), 1);
    const curve = Math.min(Math.max(distance * 0.18, 34), 92);
    const control = {
      x: (start.x + end.x) / 2 - (dy / distance) * curve,
      y: (start.y + end.y) / 2 + (dx / distance) * curve
    };
    return {
      path: `M ${start.x} ${start.y} Q ${control.x} ${control.y} ${end.x} ${end.y}`,
      label: pointOnQuadratic(start, control, end, 0.5)
    };
  }

  if (type === "elbow") {
    const midX = (start.x + end.x) / 2;
    return {
      path: `M ${start.x} ${start.y} L ${midX} ${start.y} L ${midX} ${end.y} L ${end.x} ${end.y}`,
      label: { x: midX, y: (start.y + end.y) / 2 }
    };
  }

  return {
    path: `M ${start.x} ${start.y} L ${end.x} ${end.y}`,
    label: { x: (start.x + end.x) / 2, y: (start.y + end.y) / 2 }
  };
}

function centerOf(node) {
  return { x: node.x + node.width / 2, y: node.y + node.height / 2 };
}

function pointOnRectEdge(node, toward) {
  const center = centerOf(node);
  const dx = toward.x - center.x;
  const dy = toward.y - center.y;
  if (dx === 0 && dy === 0) return center;

  const halfWidth = Math.max(node.width / 2, 1);
  const halfHeight = Math.max(node.height / 2, 1);
  const scale = Math.min(
      Math.abs(dx) < 0.001 ? Number.POSITIVE_INFINITY : halfWidth / Math.abs(dx),
      Math.abs(dy) < 0.001 ? Number.POSITIVE_INFINITY : halfHeight / Math.abs(dy)
  );

  return {
    x: center.x + dx * scale,
    y: center.y + dy * scale
  };
}

function pointOnQuadratic(start, control, end, t) {
  const inverse = 1 - t;
  return {
    x: inverse * inverse * start.x + 2 * inverse * t * control.x + t * t * end.x,
    y: inverse * inverse * start.y + 2 * inverse * t * control.y + t * t * end.y
  };
}

function parseJson(value) {
  try {
    return JSON.parse(value || "{}");
  } catch {
    return {};
  }
}

export default App;
