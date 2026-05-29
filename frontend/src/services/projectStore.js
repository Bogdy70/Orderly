const XML_PATH = "/data/projects.xml";
const CUSTOM_STORAGE_KEY = "orderlyCustomProjects";
const USER_STORAGE_KEY = "orderlyUser";

export function loadStoredUser() {
  return localStorage.getItem(USER_STORAGE_KEY) || "";
}

export function saveStoredUser(username) {
  localStorage.setItem(USER_STORAGE_KEY, username);
}

export function clearStoredUser() {
  localStorage.removeItem(USER_STORAGE_KEY);
}

export async function loadProjects() {
  const customProjects = loadCustomProjects().map(project => normalizeProject(project, "custom"));

  try {
    const xmlProjects = await loadXmlProjects();
    return [...xmlProjects, ...customProjects];
  } catch (error) {
    return customProjects;
  }
}

export function loadCustomProjects() {
  try {
    return JSON.parse(localStorage.getItem(CUSTOM_STORAGE_KEY) || "[]");
  } catch (error) {
    return [];
  }
}

export function saveCustomProjects(projects) {
  localStorage.setItem(CUSTOM_STORAGE_KEY, JSON.stringify(projects));
}

export function createProject(project) {
  const customProjects = loadCustomProjects();
  const nextProject = {
    id: createProjectId(customProjects),
    title: project.title,
    category: project.category,
    owner: project.owner,
    description: project.description,
    checklist: null,
    tasks: null,
    source: "custom"
  };

  saveCustomProjects([...customProjects, nextProject]);
  return normalizeProject(nextProject, "custom");
}

export function deleteCustomProject(projectId) {
  const customProjects = loadCustomProjects();
  const project = customProjects.find(item => String(item.id) === String(projectId));

  if (!project) {
    return { deleted: false, reason: "starter" };
  }

  saveCustomProjects(customProjects.filter(item => String(item.id) !== String(projectId)));
  return { deleted: true, project };
}

export function persistProject(project) {
  if (project.source !== "custom") return;

  const customProjects = loadCustomProjects();
  const index = customProjects.findIndex(item => String(item.id) === String(project.id));
  if (index === -1) return;

  customProjects[index] = normalizeProject(project, "custom");
  saveCustomProjects(customProjects);
}

export function normalizeProject(project, source = "custom") {
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

export function buildProjectXml(project) {
  const checklistBlock = Array.isArray(project.checklist)
    ? [
        "    <checklist>",
        ...project.checklist.map(item => `      <item done="${item.done ? "true" : "false"}">${escapeXml(item.text)}</item>`),
        "    </checklist>"
      ].join("\n")
    : "";

  const tableBlock = Array.isArray(project.tasks)
    ? [
        "    <table>",
        ...project.tasks.map(row => [
          "      <row>",
          `        <task>${escapeXml(row.task)}</task>`,
          `        <status>${escapeXml(row.status)}</status>`,
          `        <priority>${escapeXml(row.priority)}</priority>`,
          "      </row>"
        ].join("\n")),
        "    </table>"
      ].join("\n")
    : "";

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

export function downloadTextFile(filename, content, mimeType) {
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

export function sanitizeFileName(value) {
  return String(value)
    .trim()
    .replace(/[\\/:*?"<>|]+/g, "-")
    .replace(/\s+/g, "-")
    .toLowerCase() || "project";
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

  return Array.from(xmlDoc.getElementsByTagName("project")).map(projectNodeToObject);
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

  return normalizeProject({
    id: projectNode.getAttribute("id"),
    title: text(projectNode, "title"),
    category: text(projectNode, "category"),
    description: text(projectNode, "description"),
    owner: text(projectNode, "owner"),
    checklist,
    tasks
  }, "xml");
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

function createProjectId(customProjects) {
  const maxId = customProjects.reduce((max, project) => {
    const value = Number(String(project.id).replace(/\D/g, "")) || 0;
    return Math.max(max, value);
  }, 100);

  return String(maxId + 1);
}

function escapeXml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&apos;");
}
