import { useEffect, useMemo, useState } from "react";
import {
  buildProjectXml,
  clearStoredUser,
  createProject,
  deleteCustomProject,
  downloadTextFile,
  loadProjects,
  loadStoredUser,
  persistProject,
  sanitizeFileName,
  saveStoredUser
} from "./services/projectStore.js";

const EMPTY_PROJECT_FORM = {
  title: "",
  category: "",
  owner: "",
  description: ""
};

const EMPTY_TASK_FORM = {
  task: "",
  status: "Pending",
  priority: "Low"
};

function App() {
  const route = getRoute();

  useEffect(() => {
    document.title = route.title;
  }, [route.title]);

  if (route.page === "login") return <LoginPage />;
  if (route.page === "dashboard") return <DashboardPage />;
  if (route.page === "project") return <ProjectPage projectId={route.projectId} />;
  return <HomePage />;
}

function HomePage() {
  return (
    <>
      <PublicNav />
      <main>
        <section className="hero-section">
          <div className="page-shell hero-grid">
            <div className="hero-copy">
              <span className="eyebrow">Organization workspace</span>
              <h1>Organize your ideas, lists and next steps in one place</h1>
              <p>
                Capture thoughts as they come up, sort them into clear lists, and keep every plan easy to follow.
              </p>
              <div className="button-row">
                <a className="button primary large" href="dashboard.html">View workspace</a>
                <a className="button secondary large" href="project.html?id=1">See sample space</a>
              </div>
            </div>

            <div className="workspace-preview" aria-label="Orderly preview">
              <div className="preview-header">
                <div>
                  <span className="muted-label">Today</span>
                  <strong>Launch Week Plan</strong>
                </div>
                <span className="status-dot">3 active</span>
              </div>
              <div className="preview-list">
                <PreviewItem checked label="Confirm launch goals" />
                <PreviewItem checked label="Collect final assets" />
                <PreviewItem label="Review launch timeline" />
                <PreviewItem label="Send kickoff update" />
              </div>
              <div className="preview-table">
                <div className="table-row header">
                  <span>Action</span>
                  <span>Status</span>
                  <span>Priority</span>
                </div>
                <div className="table-row">
                  <span>Review blockers</span>
                  <Pill tone="blue">In progress</Pill>
                  <Pill tone="red">High</Pill>
                </div>
                <div className="table-row">
                  <span>Prepare recap</span>
                  <Pill tone="gray">Pending</Pill>
                  <Pill tone="cyan">Low</Pill>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="band">
          <div className="page-shell info-grid">
            <InfoBlock title="Collect everything">
              Bring ideas, reminders and unfinished thoughts into one clear starting point.
            </InfoBlock>
            <InfoBlock title="Shape a plan">
              Break bigger goals into lists, tasks and priorities that feel manageable.
            </InfoBlock>
            <InfoBlock title="Keep moving">
              Pick up where you left off and keep your work organized without extra clutter.
            </InfoBlock>
          </div>
        </section>
      </main>
      <Footer />
    </>
  );
}

function LoginPage() {
  const [form, setForm] = useState({ username: "", password: "" });

  function handleSubmit(event) {
    event.preventDefault();

    const username = form.username.trim();
    const password = form.password.trim();

    if (!username || !password) {
      alert("Please fill in both fields.");
      return;
    }

    saveStoredUser(username);
    window.location.href = "dashboard.html";
  }

  return (
    <>
      <PublicNav compact />
      <main className="center-page">
        <section className="auth-panel">
          <div className="panel-heading centered">
            <h1>Open your workspace</h1>
            <p>Sign in to continue organizing your ideas and priorities.</p>
          </div>

          <form className="stacked-form" onSubmit={handleSubmit}>
            <label>
              <span>Name</span>
              <input
                type="text"
                placeholder="Your name"
                value={form.username}
                onChange={event => setForm({ ...form, username: event.target.value })}
                required
              />
            </label>

            <label>
              <span>Password</span>
              <input
                type="password"
                placeholder="Password"
                value={form.password}
                onChange={event => setForm({ ...form, password: event.target.value })}
                required
              />
            </label>

            <button className="button primary full-width" type="submit">Log in</button>
          </form>
        </section>
      </main>
    </>
  );
}

function DashboardPage() {
  const [projects, setProjects] = useState([]);
  const [query, setQuery] = useState("");
  const [form, setForm] = useState(EMPTY_PROJECT_FORM);
  const [loadError, setLoadError] = useState(false);

  async function refreshProjects() {
    try {
      setProjects(await loadProjects());
      setLoadError(false);
    } catch (error) {
      setLoadError(true);
    }
  }

  useEffect(() => {
    refreshProjects();
  }, []);

  const filteredProjects = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) return projects;

    return projects.filter(project =>
      project.title.toLowerCase().includes(normalizedQuery) ||
      project.category.toLowerCase().includes(normalizedQuery)
    );
  }, [projects, query]);

  function handleCreateProject(event) {
    event.preventDefault();

    const nextProject = {
      title: form.title.trim(),
      category: form.category.trim(),
      owner: form.owner.trim(),
      description: form.description.trim()
    };

    if (!nextProject.title || !nextProject.category || !nextProject.owner || !nextProject.description) {
      alert("Please fill in all fields.");
      return;
    }

    createProject(nextProject);
    setForm(EMPTY_PROJECT_FORM);
    refreshProjects();
  }

  function handleDeleteProject(project) {
    if (project.source !== "custom") {
      alert("Only spaces you created can be deleted.");
      return;
    }

    if (!confirm(`Delete space "${project.title}"?`)) return;

    deleteCustomProject(project.id);
    refreshProjects();
  }

  return (
    <>
      <WorkspaceNav />
      <main className="workspace-page">
        <div className="page-shell">
          <div className="page-heading with-actions">
            <div>
              <h1>Your spaces</h1>
              <p>Keep ideas, lists and priorities organized in one clear workspace.</p>
            </div>
            <input
              className="search-input"
              type="search"
              placeholder="Search by name or category..."
              value={query}
              onChange={event => setQuery(event.target.value)}
            />
          </div>

          {loadError && <Alert tone="warning">Spaces could not be loaded.</Alert>}

          <section className="panel">
            <div className="panel-heading">
              <h2>Create a new space</h2>
              <p>Start with a blank space, then add a checklist or action list when you are ready.</p>
            </div>

            <form className="project-form" onSubmit={handleCreateProject}>
              <label>
                <span>Space name</span>
                <input
                  type="text"
                  value={form.title}
                  onChange={event => setForm({ ...form, title: event.target.value })}
                  required
                />
              </label>
              <label>
                <span>Category</span>
                <input
                  type="text"
                  placeholder="Planning, Personal, Team..."
                  value={form.category}
                  onChange={event => setForm({ ...form, category: event.target.value })}
                  required
                />
              </label>
              <label>
                <span>Lead</span>
                <input
                  type="text"
                  value={form.owner}
                  onChange={event => setForm({ ...form, owner: event.target.value })}
                  required
                />
              </label>
              <label className="wide">
                <span>Summary</span>
                <textarea
                  rows="3"
                  value={form.description}
                  onChange={event => setForm({ ...form, description: event.target.value })}
                  required
                />
              </label>
              <button className="button primary form-action" type="submit">Create space</button>
            </form>
          </section>

          <ProjectGrid projects={filteredProjects} onDelete={handleDeleteProject} />
        </div>
      </main>
    </>
  );
}

function ProjectPage({ projectId }) {
  const [project, setProject] = useState(null);
  const [loadState, setLoadState] = useState("loading");
  const [checklistForm, setChecklistForm] = useState({ text: "", editingIndex: null });
  const [taskForm, setTaskForm] = useState({ ...EMPTY_TASK_FORM, editingIndex: null });

  useEffect(() => {
    async function loadProject() {
      try {
        const projects = await loadProjects();
        const selectedProject = projects.find(item => String(item.id) === String(projectId));

        if (!selectedProject) {
          setLoadState("not-found");
          return;
        }

        setProject(selectedProject);
        setLoadState("ready");
        document.title = `Orderly | ${selectedProject.title}`;
      } catch (error) {
        setLoadState("error");
      }
    }

    loadProject();
  }, [projectId]);

  function updateProject(updater) {
    setProject(currentProject => {
      const updatedProject = updater(structuredClone(currentProject));
      persistProject(updatedProject);
      return updatedProject;
    });
  }

  if (loadState === "loading") {
    return (
      <>
        <ProjectNav />
        <main className="workspace-page">
          <div className="page-shell">
            <Alert>Opening space...</Alert>
          </div>
        </main>
      </>
    );
  }

  if (loadState === "not-found" || loadState === "error") {
    return (
      <>
        <ProjectNav />
        <main className="workspace-page">
          <div className="page-shell">
            <Alert tone="danger">
              {loadState === "not-found" ? "Space not found." : "This space could not be opened."}
            </Alert>
          </div>
        </main>
      </>
    );
  }

  const isCustom = project.source === "custom";
  const hasChecklist = Array.isArray(project.checklist);
  const hasTasks = Array.isArray(project.tasks);

  function addChecklistSection() {
    if (!isCustom || hasChecklist) return;
    updateProject(currentProject => ({ ...currentProject, checklist: [] }));
  }

  function addTaskSection() {
    if (!isCustom || hasTasks) return;
    updateProject(currentProject => ({ ...currentProject, tasks: [] }));
  }

  function handleChecklistSubmit(event) {
    event.preventDefault();
    const text = checklistForm.text.trim();
    if (!text || !hasChecklist) return;

    updateProject(currentProject => {
      if (checklistForm.editingIndex !== null) {
        currentProject.checklist[checklistForm.editingIndex].text = text;
      } else {
        currentProject.checklist.push({ text, done: false });
      }
      return currentProject;
    });
    resetChecklistForm();
  }

  function resetChecklistForm() {
    setChecklistForm({ text: "", editingIndex: null });
  }

  function handleTaskSubmit(event) {
    event.preventDefault();
    const task = taskForm.task.trim();
    if (!task || !hasTasks) return;

    const row = {
      task,
      status: taskForm.status,
      priority: taskForm.priority
    };

    updateProject(currentProject => {
      if (taskForm.editingIndex !== null) {
        currentProject.tasks[taskForm.editingIndex] = row;
      } else {
        currentProject.tasks.push(row);
      }
      return currentProject;
    });
    resetTaskForm();
  }

  function resetTaskForm() {
    setTaskForm({ ...EMPTY_TASK_FORM, editingIndex: null });
  }

  function exportProject() {
    try {
      const xml = buildProjectXml(project);
      downloadTextFile(`${sanitizeFileName(project.title)}-${project.id}.xml`, xml, "application/xml;charset=utf-8");
    } catch (error) {
      alert("Could not export this space.");
    }
  }

  return (
    <>
      <ProjectNav />
      <main className="workspace-page">
        <div className="page-shell">
          <section className="space-header">
            <div>
              <div className="badge-row">
                <Pill tone="orange">{project.category}</Pill>
                {isCustom && <Pill tone="amber">Editable space</Pill>}
              </div>
              <h1>{project.title}</h1>
              <p>{project.description}</p>
            </div>
            <aside className="space-meta">
              <span className="muted-label">Lead</span>
              <strong>{project.owner}</strong>
              <span className="muted-label">Space type</span>
              <strong>{isCustom ? "Personal space" : "Starter space"}</strong>
              <button className="button secondary small" type="button" onClick={exportProject}>Export space</button>
            </aside>
          </section>

          {isCustom && (
            <section className="panel setup-panel">
              <div className="panel-heading">
                <h2>Set up this space</h2>
                <p>Choose the first section you want to add to this space.</p>
              </div>
              <div className="button-row">
                <button className="button secondary" type="button" onClick={addChecklistSection} disabled={hasChecklist}>
                  Add checklist
                </button>
                <button className="button secondary" type="button" onClick={addTaskSection} disabled={hasTasks}>
                  Add action list
                </button>
              </div>
            </section>
          )}

          <div className={`section-grid ${hasChecklist && !hasTasks ? "single" : ""} ${hasTasks && !hasChecklist ? "single" : ""}`}>
            {hasChecklist && (
              <section className="panel">
                <div className="panel-heading inline">
                  <h2>Checklist</h2>
                </div>

                {isCustom && (
                  <form className="inline-form" onSubmit={handleChecklistSubmit}>
                    <input
                      type="text"
                      placeholder="Add checklist item"
                      value={checklistForm.text}
                      onChange={event => setChecklistForm({ ...checklistForm, text: event.target.value })}
                    />
                    <button className="button primary" type="submit">
                      {checklistForm.editingIndex !== null ? "Save" : "Add"}
                    </button>
                    {checklistForm.editingIndex !== null && (
                      <button className="button ghost" type="button" onClick={resetChecklistForm}>Cancel edit</button>
                    )}
                  </form>
                )}

                {project.checklist.length === 0 && <Alert>No checklist items yet.</Alert>}

                <ul className="checklist">
                  {project.checklist.map((item, index) => (
                    <li key={`${item.text}-${index}`}>
                      <label className="check-row">
                        <input
                          type="checkbox"
                          checked={item.done}
                          disabled={!isCustom}
                          onChange={() => updateProject(currentProject => {
                            currentProject.checklist[index].done = !currentProject.checklist[index].done;
                            return currentProject;
                          })}
                        />
                        <span className={item.done ? "done" : ""}>{item.text}</span>
                      </label>
                      {isCustom && (
                        <div className="row-actions">
                          <button
                            className="icon-button"
                            type="button"
                            title="Edit item"
                            aria-label="Edit item"
                            onClick={() => setChecklistForm({ text: item.text, editingIndex: index })}
                          >
                            Edit
                          </button>
                          <button
                            className="icon-button danger"
                            type="button"
                            title="Delete item"
                            aria-label="Delete item"
                            onClick={() => {
                              updateProject(currentProject => {
                                currentProject.checklist.splice(index, 1);
                                return currentProject;
                              });
                              resetChecklistForm();
                            }}
                          >
                            Delete
                          </button>
                        </div>
                      )}
                    </li>
                  ))}
                </ul>
              </section>
            )}

            {hasTasks && (
              <section className="panel wide-panel">
                <div className="panel-heading inline">
                  <h2>Action List</h2>
                </div>

                {isCustom && (
                  <form className="task-form" onSubmit={handleTaskSubmit}>
                    <input
                      type="text"
                      placeholder="Action item"
                      value={taskForm.task}
                      onChange={event => setTaskForm({ ...taskForm, task: event.target.value })}
                    />
                    <select
                      value={taskForm.status}
                      onChange={event => setTaskForm({ ...taskForm, status: event.target.value })}
                    >
                      <option>Pending</option>
                      <option>In progress</option>
                      <option>Done</option>
                    </select>
                    <select
                      value={taskForm.priority}
                      onChange={event => setTaskForm({ ...taskForm, priority: event.target.value })}
                    >
                      <option>Low</option>
                      <option>Medium</option>
                      <option>High</option>
                    </select>
                    <button className="button primary" type="submit">
                      {taskForm.editingIndex !== null ? "Save" : "Add"}
                    </button>
                    {taskForm.editingIndex !== null && (
                      <button className="button ghost" type="button" onClick={resetTaskForm}>Cancel edit</button>
                    )}
                  </form>
                )}

                {project.tasks.length === 0 && <Alert>No action items yet.</Alert>}

                <div className="responsive-table">
                  <table>
                    <thead>
                      <tr>
                        <th>Action</th>
                        <th>Status</th>
                        <th>Priority</th>
                        <th className="actions-column">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {project.tasks.map((row, index) => (
                        <tr key={`${row.task}-${index}`}>
                          <td>{row.task}</td>
                          <td><Pill tone={statusTone(row.status)}>{row.status}</Pill></td>
                          <td><Pill tone={priorityTone(row.priority)}>{row.priority}</Pill></td>
                          <td>
                            {isCustom && (
                              <div className="table-actions">
                                <button
                                  className="button secondary small"
                                  type="button"
                                  onClick={() => setTaskForm({ ...row, editingIndex: index })}
                                >
                                  Edit
                                </button>
                                <button
                                  className="button danger small"
                                  type="button"
                                  onClick={() => {
                                    updateProject(currentProject => {
                                      currentProject.tasks.splice(index, 1);
                                      return currentProject;
                                    });
                                    resetTaskForm();
                                  }}
                                >
                                  Delete
                                </button>
                              </div>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            )}
          </div>
        </div>
      </main>
    </>
  );
}

function ProjectGrid({ projects, onDelete }) {
  if (!projects.length) {
    return (
      <section className="empty-state">
        <h2>No spaces found</h2>
        <p>Try another search term or create a new space.</p>
      </section>
    );
  }

  return (
    <section className="project-grid">
      {projects.map(project => (
        <article className="project-card" key={`${project.source}-${project.id}`}>
          <div className="card-topline">
            <Pill tone="orange">{project.category}</Pill>
            <span className="muted-label">
              {project.source === "custom" ? "Your space" : "Starter space"} #{project.id}
            </span>
          </div>
          <h2>{project.title}</h2>
          <p>{project.description}</p>
          <div className="card-owner">Lead: <strong>{project.owner}</strong></div>
          <div className="card-actions">
            <a className="button primary" href={`project.html?id=${encodeURIComponent(project.id)}`}>Open space</a>
            {project.source === "custom" && (
              <button className="button danger" type="button" onClick={() => onDelete(project)}>Delete</button>
            )}
          </div>
        </article>
      ))}
    </section>
  );
}

function PublicNav({ compact = false }) {
  return (
    <header className="topbar">
      <nav className="page-shell nav-content">
        <a className="brand" href="index.html">Orderly</a>
        <div className="nav-actions">
          {compact ? (
            <a className="button secondary small" href="index.html">Back</a>
          ) : (
            <>
              <a className="button secondary small" href="login.html">Log in</a>
              <a className="button primary small" href="dashboard.html">Open workspace</a>
            </>
          )}
        </div>
      </nav>
    </header>
  );
}

function WorkspaceNav() {
  const username = loadStoredUser() || "Guest";

  function logout() {
    clearStoredUser();
    window.location.href = "index.html";
  }

  return (
    <header className="topbar">
      <nav className="page-shell nav-content">
        <a className="brand" href="index.html">Orderly</a>
        <div className="nav-actions">
          <span className="hello">Hello, <strong>{username}</strong></span>
          <button className="button danger small" type="button" onClick={logout}>Log out</button>
        </div>
      </nav>
    </header>
  );
}

function ProjectNav() {
  return (
    <header className="topbar">
      <nav className="page-shell nav-content">
        <a className="brand" href="index.html">Orderly</a>
        <a className="button secondary small" href="dashboard.html">Back to workspace</a>
      </nav>
    </header>
  );
}

function Footer() {
  return (
    <footer className="footer">
      <div className="page-shell footer-content">
        <span>Orderly</span>
        <span>Clear space for plans, notes and priorities.</span>
      </div>
    </footer>
  );
}

function InfoBlock({ title, children }) {
  return (
    <article className="info-block">
      <h2>{title}</h2>
      <p>{children}</p>
    </article>
  );
}

function PreviewItem({ checked = false, label }) {
  return (
    <div className="preview-item">
      <span className={checked ? "fake-checkbox checked" : "fake-checkbox"} />
      <span className={checked ? "done" : ""}>{label}</span>
    </div>
  );
}

function Pill({ tone = "gray", children }) {
  return <span className={`pill ${tone}`}>{children}</span>;
}

function Alert({ tone = "neutral", children }) {
  return <div className={`alert ${tone}`}>{children}</div>;
}

function getRoute() {
  const path = window.location.pathname.toLowerCase();
  const params = new URLSearchParams(window.location.search);

  if (path.endsWith("/login.html")) {
    return { page: "login", title: "Orderly | Sign In" };
  }

  if (path.endsWith("/dashboard.html")) {
    return { page: "dashboard", title: "Orderly | Your Spaces" };
  }

  if (path.endsWith("/project.html")) {
    return {
      page: "project",
      title: "Orderly | Space",
      projectId: params.get("id") || "1"
    };
  }

  return { page: "home", title: "Orderly | Home" };
}

function statusTone(status) {
  const normalized = String(status).toLowerCase();
  if (normalized.includes("done")) return "green";
  if (normalized.includes("progress")) return "blue";
  return "gray";
}

function priorityTone(priority) {
  const normalized = String(priority).toLowerCase();
  if (normalized === "high") return "red";
  if (normalized === "medium") return "amber";
  return "cyan";
}

export default App;
