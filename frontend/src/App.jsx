import { useEffect, useMemo, useRef, useState } from "react";
import {
  clearToken,
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
  ensureLocalUser,
  getCurrentUser,
  getSpaceFull,
  isAuthenticated,
  listSpaces,
  login,
  registerAccount,
  updateBlock,
  updateChecklistItem,
  updateDiagramNode,
  updateTableRow
} from "./services/api.js";

const EMPTY_SPACE = { name: "", description: "", icon: "folder", color: "#2563eb" };
const EMPTY_BLOCK = { type: "checklist", title: "", position: 1 };
const SHAPES = ["task", "note", "decision", "milestone"];
const BLOCK_SIZES = {
  checklist: { width: 560, height: 360 },
  table: { width: 820, height: 400 },
  diagram: { width: 780, height: 620 }
};

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
  const [form, setForm] = useState({ email: "", username: "", password: "" });
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    try {
      await registerAccount(form);
      setMessage("Account created. You can log in now.");
      setForm({ email: "", username: "", password: "" });
    } catch (error) {
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
        <form className="form-stack" onSubmit={submit}>
          <Field label="Email" value={form.email} onChange={email => setForm({ ...form, email })} type="email" />
          <Field label="Username" value={form.username} onChange={username => setForm({ ...form, username })} />
          <Field label="Password" value={form.password} onChange={password => setForm({ ...form, password })} type="password" />
          <button className="button primary" disabled={busy}>{busy ? "Creating..." : "Register"}</button>
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
      await ensureLocalUser();
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
  const [layout, setLayout] = useState({});
  const [drag, setDrag] = useState(null);
  const boardRef = useRef(null);

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
    const defaults = defaultBlockLayout(blockForm.type, blocks.length);
    const block = await createBlock(spaceId, { ...blockForm, title, position, ...defaults });
    if (block.type === "diagram") await createDiagram(block.id);
    setBlockForm(EMPTY_BLOCK);
    refresh();
  }

  function positionFor(item, index) {
    const fallback = defaultBlockLayout(item.block.type, index);
    return layout[item.block.id] || {
      x: numberOr(item.block.x, fallback.x),
      y: numberOr(item.block.y, fallback.y)
    };
  }

  function sizeFor(block) {
    const fallback = BLOCK_SIZES[block.type] || BLOCK_SIZES.checklist;
    return {
      width: numberOr(block.width, fallback.width),
      height: numberOr(block.height, fallback.height)
    };
  }

  const boardSize = useMemo(() => {
    return blocks.reduce((size, item, index) => {
      const pos = positionFor(item, index);
      const blockSize = sizeFor(item.block);
      return {
        minWidth: Math.max(size.minWidth, pos.x + blockSize.width + 48),
        minHeight: Math.max(size.minHeight, pos.y + blockSize.height + 48)
      };
    }, { minWidth: 1180, minHeight: 760 });
  }, [blocks, layout]);

  function startBlockDrag(event, item, index) {
    if (event.button !== 0) return;
    const point = positionFor(item, index);
    event.currentTarget.setPointerCapture?.(event.pointerId);
    setDrag({ id: item.block.id, offsetX: event.clientX - point.x, offsetY: event.clientY - point.y });
  }

  function moveBlock(event) {
    if (!drag) return;
    const board = boardRef.current;
    const rect = board.getBoundingClientRect();
    const next = {
      ...layout,
      [drag.id]: {
        x: Math.max(0, event.clientX - rect.left + board.scrollLeft - drag.offsetX),
        y: Math.max(0, event.clientY - rect.top + board.scrollTop - drag.offsetY)
      }
    };
    setLayout(next);
  }

  async function finishBlockDrag() {
    if (!drag) return;
    const id = drag.id;
    const current = layout[id];
    setDrag(null);
    if (!current) return;
    await updateBlock(id, { x: current.x, y: current.y });
    setLayout(previous => {
      const next = { ...previous };
      delete next[id];
      return next;
    });
    refresh();
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

      <section className="block-board" ref={boardRef} onPointerMove={moveBlock} onPointerUp={finishBlockDrag} onPointerCancel={() => setDrag(null)}>
        <div className="block-board-surface" style={boardSize} />
        {blocks.map((item, index) => {
          const pos = positionFor(item, index);
          const blockSize = sizeFor(item.block);
          return (
            <article className="block-card" key={item.block.id} style={{ left: pos.x, top: pos.y, width: blockSize.width, minHeight: blockSize.height }}>
              <header className="block-header" onPointerDown={event => startBlockDrag(event, item, index)}>
                <div>
                  <span>{item.block.type}</span>
                  <h2>{item.block.title}</h2>
                </div>
                <button className="button danger small" onPointerDown={event => event.stopPropagation()} onClick={() => deleteBlock(item.block.id).then(refresh)}>Delete</button>
              </header>
              {item.block.type === "checklist" && <ChecklistBlock block={item.block} items={item.content || []} refresh={refresh} />}
              {item.block.type === "table" && <TableBlock block={item.block} rows={item.content || []} refresh={refresh} />}
              {item.block.type === "diagram" && <DiagramBlock block={item.block} content={item.content} refresh={refresh} />}
            </article>
          );
        })}
      </section>
    </AppShell>
  );
}

function ChecklistBlock({ block, items, refresh }) {
  const [text, setText] = useState("");
  return (
    <div className="block-body">
      <form className="inline-form" onSubmit={async event => {
        event.preventDefault();
        if (!text.trim()) return;
        await createChecklistItem(block.id, { text, done: false, position: items.length + 1 });
        setText("");
        refresh();
      }}>
        <input placeholder="Checklist item" value={text} onChange={event => setText(event.target.value)} />
        <button className="button primary small">Add</button>
      </form>
      <ul className="checklist">
        {items.map(item => (
          <li key={item.id}>
            <label>
              <input type="checkbox" checked={item.done} onChange={() => updateChecklistItem(item.id, { done: !item.done }).then(refresh)} />
              <span className={item.done ? "done" : ""}>{item.text}</span>
            </label>
            <button className="text-danger" onClick={() => deleteChecklistItem(item.id).then(refresh)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

function TableBlock({ block, rows, refresh }) {
  const [form, setForm] = useState({ title: "", status: "todo", priority: "", dueDate: "" });
  async function submit(event) {
    event.preventDefault();
    if (!form.title.trim()) return;
    await createTableRow(block.id, { ...form, dueDate: form.dueDate || null, position: rows.length + 1 });
    setForm({ title: "", status: "todo", priority: "", dueDate: "" });
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
        <input placeholder="Priority" value={form.priority || ""} onChange={event => setForm({ ...form, priority: event.target.value })} />
        <input type="date" value={form.dueDate || ""} onChange={event => setForm({ ...form, dueDate: event.target.value })} />
        <button className="button primary small">Add</button>
      </form>
      <div className="rows">
        {rows.map(row => (
          <div className="data-row" key={row.id}>
            <input value={row.title} onChange={event => updateTableRow(row.id, { title: event.target.value }).then(refresh)} />
            <span>{row.status}</span>
            <span>{row.priority || "none"}</span>
            <span>{row.dueDate || "no date"}</span>
            <button className="text-danger" onClick={() => deleteTableRow(row.id).then(refresh)}>Delete</button>
          </div>
        ))}
      </div>
    </div>
  );
}

function DiagramBlock({ block, content, refresh }) {
  const [selectedNode, setSelectedNode] = useState(null);
  const [pendingNode, setPendingNode] = useState(null);
  const [selectedEdges, setSelectedEdges] = useState(new Set());
  const [drag, setDrag] = useState(null);
  const [draft, setDraft] = useState({ label: "", type: "task", color: "#2563eb" });
  const diagram = content?.diagram;
  const nodes = content?.nodes || [];
  const edges = content?.edges || [];

  useEffect(() => {
    function onKeyDown(event) {
      if (event.key === "Backspace" && selectedEdges.size) {
        event.preventDefault();
        deleteSelectedEdges();
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [selectedEdges, edges]);

  useEffect(() => {
    if (!selectedNode) return;
    const node = nodes.find(item => item.id === selectedNode);
    if (!node) return;
    const style = parseJson(node.styleJson);
    setDraft({ label: node.label, type: node.type || "task", color: style.color || "#2563eb" });
  }, [selectedNode]);

  async function ensureDiagram() {
    if (diagram) return diagram;
    await createDiagram(block.id);
    await refresh();
    return null;
  }

  async function addNode() {
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

  async function connectNode(nodeId) {
    setSelectedNode(nodeId);
    if (!pendingNode || pendingNode === nodeId) {
      setPendingNode(nodeId);
      return;
    }
    await createDiagramEdge(diagram.id, { sourceNodeId: pendingNode, targetNodeId: nodeId, type: "arrow", label: "", styleJson: "{}" });
    setPendingNode(null);
    refresh();
  }

  async function saveNode() {
    if (!selectedNode) return;
    await updateDiagramNode(selectedNode, {
      label: draft.label,
      type: draft.type,
      styleJson: JSON.stringify({ color: draft.color })
    });
    refresh();
  }

  async function removeNode() {
    if (!selectedNode) return;
    await deleteDiagramNode(selectedNode);
    setSelectedNode(null);
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
        <button className="button primary small" onClick={addNode}>Add shape</button>
        <button className="button secondary small" disabled={!selectedNode} onClick={saveNode}>Save shape</button>
        <button className="button danger small" disabled={!selectedNode} onClick={removeNode}>Delete shape</button>
        <button className="button danger small" disabled={!selectedEdges.size} onClick={deleteSelectedEdges}>Delete arrow</button>
      </div>
      <div className="diagram-canvas" onPointerMove={event => {
        if (!drag) return;
        const rect = event.currentTarget.getBoundingClientRect();
        setDrag({ ...drag, x: event.clientX - rect.left - drag.offsetX, y: event.clientY - rect.top - drag.offsetY });
      }} onPointerUp={endDrag}>
        <svg className="edge-layer">
          <defs>
            <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto">
              <path d="M0,0 L0,6 L9,3 z" fill="#334155" />
            </marker>
          </defs>
          {edges.map(edge => {
            const source = renderedNodes.find(node => node.id === edge.sourceNodeId);
            const target = renderedNodes.find(node => node.id === edge.targetNodeId);
            if (!source || !target) return null;
            const selected = selectedEdges.has(edge.id);
            return (
              <line
                key={edge.id}
                x1={source.x + source.width / 2}
                y1={source.y + source.height / 2}
                x2={target.x + target.width / 2}
                y2={target.y + target.height / 2}
                className={selected ? "edge selected" : "edge"}
                markerEnd="url(#arrow)"
                onClick={event => {
                  event.stopPropagation();
                  setSelectedEdges(new Set([edge.id]));
                }}
              />
            );
          })}
        </svg>
        {renderedNodes.map(node => {
          const style = parseJson(node.styleJson);
          return (
            <button
              key={node.id}
              className={`diagram-node ${node.type} ${selectedNode === node.id ? "selected" : ""} ${pendingNode === node.id ? "pending" : ""}`}
              style={{ left: node.x, top: node.y, width: node.width, height: node.height, borderColor: style.color || "#2563eb" }}
              onClick={() => connectNode(node.id)}
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

function Field({ label, value, onChange, type = "text" }) {
  return (
    <label>
      <span>{label}</span>
      <input type={type} value={value} onChange={event => onChange(event.target.value)} required />
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

function defaultBlockLayout(type, index) {
  const size = BLOCK_SIZES[type] || BLOCK_SIZES.checklist;
  return {
    x: 32 + (index % 2) * 680,
    y: 32 + Math.floor(index / 2) * 460,
    width: size.width,
    height: size.height
  };
}

function numberOr(value, fallback) {
  return Number.isFinite(value) ? value : fallback;
}

function parseJson(value) {
  try {
    return JSON.parse(value || "{}");
  } catch {
    return {};
  }
}

export default App;
