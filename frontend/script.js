const XML_PATH = "data/projects.xml";
const CUSTOM_STORAGE_KEY = "orderlyCustomProjects";
const USER_STORAGE_KEY = "orderlyUser";

document.addEventListener("DOMContentLoaded", () => {
  setupLoginPage();
  setupDashboardPage();
  setupProjectPage();
  setupNavbarUser();
  setupLogout();
});

function setPageTitle(value) {
  document.title = value ? `Orderly | ${value}` : "Orderly";
}

function setupLoginPage() {
  const form = document.getElementById("login-form");
  if (!form) return;

  form.addEventListener("submit", (event) => {
    event.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!username || !password) {
      alert("Please fill in both fields.");
      return;
    }

    localStorage.setItem(USER_STORAGE_KEY, username);
    window.location.href = "dashboard.html";
  });
}

function setupNavbarUser() {
  const navUsername = document.getElementById("nav-username");
  if (!navUsername) return;
  navUsername.textContent = loadStoredUser() || "Guest";
}

function setupLogout() {
  const logoutBtn = document.getElementById("logout-btn");
  if (!logoutBtn) return;

  logoutBtn.addEventListener("click", () => {
    localStorage.removeItem(USER_STORAGE_KEY);
    window.location.href = "index.html";
  });
}

function loadStoredUser() {
  return localStorage.getItem(USER_STORAGE_KEY) || "";
}

function normalizeChecklist(value) {
  if (value === null) return null;
  if (!Array.isArray(value)) return [];
  return value.map(item => ({
    text: item?.text ?? "",
    done: item?.done === true
  }));
}

function normalizeTasks(value) {
  if (value === null) return null;
  if (!Array.isArray(value)) return [];
  return value.map(row => ({
    task: row?.task ?? "",
    status: row?.status ?? "Pending",
    priority: row?.priority ?? "Low"
  }));
}

function normalizeProject(project, source = "custom") {
  return {
    id: String(project?.id ?? ""),
    title: project?.title ?? "Untitled space",
    category: project?.category ?? "General",
    owner: project?.owner ?? "Unknown",
    description: project?.description ?? "",
    checklist: normalizeChecklist(project?.checklist),
    tasks: normalizeTasks(project?.tasks),
    source
  };
}

async function loadProjects() {
  const customProjects = loadCustomProjects().map(project => normalizeProject(project, "custom"));

  try {
    const xmlProjects = await loadXmlProjects();
    return [...xmlProjects, ...customProjects];
  } catch (error) {
    return customProjects;
  }
}

async function loadXmlProjects() {
  const response = await fetch(XML_PATH);

  if (!response.ok) {
    throw new Error("Starter data could not be loaded.");
  }

  const xmlText = await response.text();
  const parser = new DOMParser();
  const xmlDoc = parser.parseFromString(xmlText, "application/xml");
  const parserError = xmlDoc.querySelector("parsererror");

  if (parserError) {
    throw new Error("Invalid XML format.");
  }

  const projectNodes = Array.from(xmlDoc.getElementsByTagName("project"));
  return projectNodes.map(projectNodeToObject);
}

function loadCustomProjects() {
  try {
    return JSON.parse(localStorage.getItem(CUSTOM_STORAGE_KEY) || "[]");
  } catch (error) {
    return [];
  }
}

function saveCustomProjects(projects) {
  localStorage.setItem(CUSTOM_STORAGE_KEY, JSON.stringify(projects));
}

function projectNodeToObject(projectNode) {
  const text = (parent, tag) => parent.getElementsByTagName(tag)[0]?.textContent?.trim() || "";

  const checklistNode = projectNode.getElementsByTagName("checklist")[0];
  const tableNode = projectNode.getElementsByTagName("table")[0];

  const checklist = Array.from(checklistNode?.getElementsByTagName("item") || []).map(item => ({
    text: item.textContent.trim(),
    done: item.getAttribute("done") === "true"
  }));

  const tasks = Array.from(tableNode?.getElementsByTagName("row") || []).map(row => ({
    task: text(row, "task"),
    status: text(row, "status"),
    priority: text(row, "priority")
  }));

  return {
    id: String(projectNode.getAttribute("id")),
    title: text(projectNode, "title"),
    category: text(projectNode, "category"),
    description: text(projectNode, "description"),
    owner: text(projectNode, "owner"),
    checklist,
    tasks,
    source: "xml"
  };
}

async function setupDashboardPage() {
  const projectList = document.getElementById("project-list");
  if (!projectList) return;
  setPageTitle("Your Spaces");

  const searchInput = document.getElementById("project-search");
  const alertBox = document.getElementById("dashboard-alert");
  const form = document.getElementById("project-form");

  let projects = [];

  async function refreshAndRender() {
    projects = await loadProjects();
    renderProjectCards(projects, projectList);
  }

  try {
    await refreshAndRender();

    searchInput?.addEventListener("input", () => {
      const query = searchInput.value.trim().toLowerCase();
      const filtered = projects.filter(project =>
        project.title.toLowerCase().includes(query) ||
        project.category.toLowerCase().includes(query)
      );
      renderProjectCards(filtered, projectList);
    });

    form?.addEventListener("submit", async (event) => {
      event.preventDefault();

      const title = document.getElementById("project-title").value.trim();
      const category = document.getElementById("project-category").value.trim();
      const owner = document.getElementById("project-owner").value.trim();
      const description = document.getElementById("project-description").value.trim();

      if (!title || !category || !owner || !description) {
        alert("Please fill in all fields.");
        return;
      }

      const customProjects = loadCustomProjects();
      customProjects.push({
        id: createProjectId(customProjects),
        title,
        category,
        owner,
        description,
        checklist: null,
        tasks: null,
        source: "custom"
      });

      saveCustomProjects(customProjects);
      form.reset();
      await refreshAndRender();
    });

    window.deleteProjectFromDashboard = async (projectId) => {
      const customProjects = loadCustomProjects();
      const project = customProjects.find(p => p.id === projectId);

      if (!project) {
        alert("Only spaces you created can be deleted.");
        return;
      }

      if (!confirm(`Delete space "${project.title}"?`)) return;

      const updated = customProjects.filter(p => p.id !== projectId);
      saveCustomProjects(updated);
      await refreshAndRender();
    };
  } catch (error) {
    if (alertBox) {
      alertBox.classList.remove("d-none");
      alertBox.textContent = "Spaces could not be loaded.";
    }
  }
}

function createProjectId(customProjects) {
  const maxId = customProjects.reduce((max, project) => {
    const value = Number(String(project.id).replace(/\D/g, "")) || 0;
    return Math.max(max, value);
  }, 100);

  return String(maxId + 1);
}

function renderProjectCards(projects, container) {
  if (!projects.length) {
    container.innerHTML = `
      <div class="col-12">
        <div class="empty-state">
          <h2 class="h4">No spaces found</h2>
          <p class="text-muted mb-0">Try another search term or create a new space.</p>
        </div>
      </div>
    `;
    return;
  }

  container.innerHTML = projects.map(project => `
    <div class="col-md-6 col-xl-4">
      <div class="card project-card">
        <div class="card-body p-4 d-flex flex-column">
          <div class="d-flex justify-content-between align-items-start gap-2 mb-3">
            <span class="badge badge-soft rounded-pill">${escapeHtml(project.category)}</span>
            <span class="text-muted small">${project.source === "custom" ? "Your space" : "Starter space"} #${escapeHtml(project.id)}</span>
          </div>
          <h2 class="h4 mb-2">${escapeHtml(project.title)}</h2>
          <p class="text-muted flex-grow-1">${escapeHtml(project.description)}</p>
          <div class="small text-muted mb-3">Lead: <strong>${escapeHtml(project.owner)}</strong></div>
          <div class="d-flex gap-2">
            <a href="project.html?id=${encodeURIComponent(project.id)}" class="btn btn-primary flex-grow-1">Open space</a>
            ${project.source === "custom"
              ? `<button class="btn btn-outline-danger" type="button" onclick="deleteProjectFromDashboard('${escapeHtml(project.id)}')">Delete</button>`
              : ""}
          </div>
        </div>
      </div>
    </div>
  `).join("");
}

async function setupProjectPage() {
  const content = document.getElementById("project-content");
  const notFound = document.getElementById("project-not-found");

  if (!content || !notFound) return;

  setPageTitle("Space");

  try {
    const params = new URLSearchParams(window.location.search);
    const projectId = params.get("id") || "1";
    const projects = await loadProjects();
    const project = projects.find(p => String(p.id) === String(projectId));

    if (!project) {
      notFound?.classList.remove("d-none");
      notFound.textContent = "Space not found.";
      setPageTitle("Space Not Found");
      return;
    }

    content?.classList.remove("d-none");
    setPageTitle(project.title);
    renderProjectPage(project);
    bindProjectPageEvents(project);
  } catch (error) {
    if (notFound) {
      notFound.classList.remove("d-none");
      notFound.textContent = "This space could not be opened.";
    }
    setPageTitle("Space");
  }
}

function bindProjectPageEvents(project) {
  const checklistForm = document.getElementById("checklist-form");
  const taskForm = document.getElementById("task-form");
  const checklistInput = document.getElementById("new-check-item");
  const checklistEditingIndex = document.getElementById("editing-checklist-index");
  const checklistSubmitBtn = document.getElementById("checklist-submit-btn");
  const checklistCancelWrap = document.getElementById("checklist-cancel-wrap");
  const cancelChecklistEditBtn = document.getElementById("cancel-checklist-edit-btn");
  const addChecklistSectionBtn = document.getElementById("add-checklist-section-btn");
  const addTaskSectionBtn = document.getElementById("add-task-section-btn");
  const cancelTaskEditBtn = document.getElementById("cancel-task-edit-btn");
  const exportProjectXmlBtn = document.getElementById("export-project-xml-btn");

  if (checklistForm) {
    checklistForm.onsubmit = (event) => {
      event.preventDefault();
      if (!Array.isArray(project.checklist)) return;

      const value = checklistInput?.value.trim();
      if (!value) return;

      if (checklistEditingIndex?.value !== "") {
        const index = Number(checklistEditingIndex.value);
        if (Number.isInteger(index) && project.checklist[index]) {
          project.checklist[index].text = value;
        }
      } else {
        project.checklist.push({ text: value, done: false });
      }

      persistProject(project);
      renderProjectPage(project);
      bindProjectPageEvents(project);
      resetChecklistForm();
    };
  }

  if (taskForm) {
    taskForm.onsubmit = (event) => {
      event.preventDefault();
      if (!Array.isArray(project.tasks)) return;

      const taskName = document.getElementById("new-task-name").value.trim();
      const status = document.getElementById("new-task-status").value;
      const priority = document.getElementById("new-task-priority").value;
      const editingIndex = document.getElementById("editing-task-index").value;

      if (!taskName) return;

      const row = { task: taskName, status, priority };

      if (editingIndex !== "") {
        project.tasks[Number(editingIndex)] = row;
      } else {
        project.tasks.push(row);
      }

      persistProject(project);
      renderProjectPage(project);
      bindProjectPageEvents(project);
      resetTaskForm();
    };
  }

  if (cancelChecklistEditBtn) {
    cancelChecklistEditBtn.onclick = () => {
      resetChecklistForm();
    };
  }

  if (cancelTaskEditBtn) {
    cancelTaskEditBtn.onclick = () => {
      resetTaskForm();
    };
  }

  if (addChecklistSectionBtn) {
    addChecklistSectionBtn.onclick = () => {
      if (project.source !== "custom" || Array.isArray(project.checklist)) return;
      project.checklist = [];
      persistProject(project);
      renderProjectPage(project);
      bindProjectPageEvents(project);
    };
  }

  if (addTaskSectionBtn) {
    addTaskSectionBtn.onclick = () => {
      if (project.source !== "custom" || Array.isArray(project.tasks)) return;
      project.tasks = [];
      persistProject(project);
      renderProjectPage(project);
      bindProjectPageEvents(project);
    };
  }

  if (exportProjectXmlBtn) {
    exportProjectXmlBtn.onclick = () => {
      exportProjectAsXml(project);
    };
  }

  window.toggleChecklistItem = (index) => {
    if (!Array.isArray(project.checklist) || !project.checklist[index]) return;
    project.checklist[index].done = !project.checklist[index].done;
    persistProject(project);
    renderProjectPage(project);
    bindProjectPageEvents(project);
  };

  window.removeChecklistItem = (index) => {
    if (!Array.isArray(project.checklist)) return;
    project.checklist.splice(index, 1);
    persistProject(project);
    renderProjectPage(project);
    bindProjectPageEvents(project);
    resetChecklistForm();
  };

  window.editChecklistItem = (index) => {
    if (!Array.isArray(project.checklist) || !project.checklist[index]) return;
    if (!checklistInput || !checklistEditingIndex || !checklistSubmitBtn || !checklistCancelWrap) return;

    checklistInput.value = project.checklist[index].text;
    checklistEditingIndex.value = String(index);
    checklistSubmitBtn.textContent = "Save";
    checklistCancelWrap.classList.remove("d-none");
    checklistInput.focus();
  };

  window.removeTaskRow = (index) => {
    if (!Array.isArray(project.tasks)) return;
    project.tasks.splice(index, 1);
    persistProject(project);
    renderProjectPage(project);
    bindProjectPageEvents(project);
    resetTaskForm();
  };

  window.editTaskRow = (index) => {
    if (!Array.isArray(project.tasks) || !project.tasks[index]) return;

    const row = project.tasks[index];
    document.getElementById("new-task-name").value = row.task;
    document.getElementById("new-task-status").value = row.status;
    document.getElementById("new-task-priority").value = row.priority;
    document.getElementById("editing-task-index").value = String(index);
    document.getElementById("task-submit-btn").textContent = "Save";
    document.getElementById("task-cancel-wrap").classList.remove("d-none");
    document.getElementById("new-task-name").focus();
  };
}

function resetChecklistForm() {
  const checklistInput = document.getElementById("new-check-item");
  const checklistEditingIndex = document.getElementById("editing-checklist-index");
  const checklistSubmitBtn = document.getElementById("checklist-submit-btn");
  const checklistCancelWrap = document.getElementById("checklist-cancel-wrap");

  if (!checklistInput || !checklistEditingIndex || !checklistSubmitBtn || !checklistCancelWrap) return;

  checklistInput.value = "";
  checklistEditingIndex.value = "";
  checklistSubmitBtn.textContent = "Add";
  checklistCancelWrap.classList.add("d-none");
}

function resetTaskForm() {
  const name = document.getElementById("new-task-name");
  const status = document.getElementById("new-task-status");
  const priority = document.getElementById("new-task-priority");
  const editingIndex = document.getElementById("editing-task-index");
  const submitBtn = document.getElementById("task-submit-btn");
  const cancelWrap = document.getElementById("task-cancel-wrap");

  if (!name || !status || !priority || !editingIndex || !submitBtn || !cancelWrap) return;

  name.value = "";
  status.value = "Pending";
  priority.value = "Low";
  editingIndex.value = "";
  submitBtn.textContent = "Add";
  cancelWrap.classList.add("d-none");
}

function renderProjectPage(project) {
  const header = document.getElementById("project-header");
  const checklist = document.getElementById("checklist-items");
  const tableBody = document.getElementById("task-table-body");
  const checklistPanelCol = document.getElementById("checklist-panel-col");
  const taskPanelCol = document.getElementById("task-panel-col");
  const checklistForm = document.getElementById("checklist-form");
  const taskForm = document.getElementById("task-form");
  const checklistEmpty = document.getElementById("checklist-empty-message");
  const tasksEmpty = document.getElementById("tasks-empty-message");
  const builderPanel = document.getElementById("project-builder-panel");

  const isCustom = project.source === "custom";
  const hasChecklist = Array.isArray(project.checklist);
  const hasTasks = Array.isArray(project.tasks);

  builderPanel?.classList.toggle("d-none", !isCustom);

  header.innerHTML = `
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
      <div>
        <span class="badge badge-soft rounded-pill mb-2">${escapeHtml(project.category)}</span>
        ${isCustom ? '<span class="badge builder-badge rounded-pill ms-2 mb-2">Editable space</span>' : ""}
        <h1 class="display-6 fw-bold mb-2">${escapeHtml(project.title)}</h1>
        <p class="text-muted mb-0">${escapeHtml(project.description)}</p>
      </div>
      <div class="bg-white border rounded-4 px-4 py-3 shadow-sm">
        <div class="small text-muted">Lead</div>
        <div class="fw-semibold">${escapeHtml(project.owner)}</div>
        <div class="small text-muted mt-2">Space type</div>
        <div class="fw-semibold">${isCustom ? "Personal space" : "Starter space"}</div>
        <button id="export-project-xml-btn" class="btn btn-outline-primary btn-sm mt-3" type="button">
          <i class="bi bi-download me-1"></i>Export space
        </button>
      </div>
    </div>
  `;

  checklistPanelCol?.classList.toggle("d-none", !hasChecklist);
  taskPanelCol?.classList.toggle("d-none", !hasTasks);

  if (checklistPanelCol) {
    checklistPanelCol.classList.remove("col-lg-5", "col-lg-12");
    checklistPanelCol.classList.add(hasChecklist && !hasTasks ? "col-lg-12" : "col-lg-5");
  }

  if (taskPanelCol) {
    taskPanelCol.classList.remove("col-lg-7", "col-lg-12");
    taskPanelCol.classList.add(hasTasks && !hasChecklist ? "col-lg-12" : "col-lg-7");
  }

  checklistForm?.classList.toggle("d-none", !hasChecklist || !isCustom);
  taskForm?.classList.toggle("d-none", !hasTasks || !isCustom);
  checklistEmpty?.classList.toggle("d-none", !hasChecklist || project.checklist.length > 0);
  tasksEmpty?.classList.toggle("d-none", !hasTasks || project.tasks.length > 0);

  if (checklist) {
    checklist.innerHTML = hasChecklist
      ? project.checklist.map((item, index) => `
        <li class="list-group-item d-flex align-items-center gap-3 px-0">
          <input class="form-check-input mt-0" type="checkbox" ${item.done ? "checked" : ""} ${isCustom ? `onchange="toggleChecklistItem(${index})"` : "disabled"} />
          <span class="flex-grow-1 ${item.done ? "check-item-done" : ""}">${escapeHtml(item.text)}</span>
          ${isCustom ? `
            <div class="d-flex align-items-center gap-2">
              <button class="inline-edit-btn" type="button" title="Edit item" aria-label="Edit item" onclick="editChecklistItem(${index})"><i class="bi bi-pencil"></i></button>
              <button class="inline-remove-btn" type="button" title="Delete item" aria-label="Delete item" onclick="removeChecklistItem(${index})"><i class="bi bi-x-lg"></i></button>
            </div>
          ` : ""}
        </li>
      `).join("")
      : "";
  }

  if (tableBody) {
    tableBody.innerHTML = hasTasks
      ? project.tasks.map((row, index) => `
        <tr>
          <td>${escapeHtml(row.task)}</td>
          <td>${statusBadge(row.status)}</td>
          <td>${priorityBadge(row.priority)}</td>
          <td class="text-end">
            ${isCustom ? `
              <div class="d-flex justify-content-end gap-2">
                <button class="btn btn-sm btn-outline-primary" type="button" onclick="editTaskRow(${index})">Edit</button>
                <button class="btn btn-sm btn-outline-danger" type="button" onclick="removeTaskRow(${index})">Delete</button>
              </div>
            ` : ""}
          </td>
        </tr>
      `).join("")
      : '<tr><td colspan="4" class="text-muted"></td></tr>';
  }
}

function persistProject(project) {
  if (project.source !== "custom") return;

  const customProjects = loadCustomProjects();
  const index = customProjects.findIndex(item => String(item.id) === String(project.id));
  if (index === -1) return;

  customProjects[index] = normalizeProject(project, "custom");
  saveCustomProjects(customProjects);
}

function statusBadge(status) {
  const normalized = String(status).toLowerCase();
  if (normalized.includes("done")) return '<span class="badge text-bg-success">Done</span>';
  if (normalized.includes("progress")) return '<span class="badge text-bg-primary">In progress</span>';
  return '<span class="badge text-bg-secondary">Pending</span>';
}

function priorityBadge(priority) {
  const normalized = String(priority).toLowerCase();
  if (normalized === "high") return '<span class="badge text-bg-danger">High</span>';
  if (normalized === "medium") return '<span class="badge text-bg-warning">Medium</span>';
  return '<span class="badge text-bg-info">Low</span>';
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeXml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&apos;");
}

function sanitizeFileName(value) {
  return String(value)
    .trim()
    .replace(/[\\/:*?"<>|]+/g, "-")
    .replace(/\s+/g, "-")
    .toLowerCase() || "project";
}

function buildProjectXml(project) {
  const checklistBlock = Array.isArray(project.checklist) ? [
    "    <checklist>",
    ...project.checklist.map(item => `      <item done="${item.done ? "true" : "false"}">${escapeXml(item.text)}</item>`),
    "    </checklist>"
  ].join("\n") : "";

  const tableBlock = Array.isArray(project.tasks) ? [
    "    <table>",
    ...project.tasks.map(row => [
      "      <row>",
      `        <task>${escapeXml(row.task)}</task>`,
      `        <status>${escapeXml(row.status)}</status>`,
      `        <priority>${escapeXml(row.priority)}</priority>`,
      "      </row>"
    ].join("\n")),
    "    </table>"
  ].join("\n") : "";

  const blocks = [checklistBlock, tableBlock].filter(Boolean).join("\n\n");

  return [
    '<?xml version="1.0" encoding="UTF-8"?>',
    "<projects>",
    `  <project id="${escapeXml(project.id)}">`,
    `    <title>${escapeXml(project.title)}</title>`,
    `    <category>${escapeXml(project.category)}</category>`,
    `    <description>${escapeXml(project.description)}</description>`,
    `    <owner>${escapeXml(project.owner)}</owner>`,
    blocks ? `\n${blocks}` : "",
    "  </project>",
    "</projects>",
    ""
  ].join("\n");
}

function downloadTextFile(filename, content, mimeType) {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");

  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
}

function exportProjectAsXml(project) {
  try {
    const xml = buildProjectXml(project);
    const fileName = `${sanitizeFileName(project.title)}-${project.id}.xml`;
    downloadTextFile(fileName, xml, "application/xml;charset=utf-8");
  } catch (error) {
    alert("Could not export this space.");
  }
}
