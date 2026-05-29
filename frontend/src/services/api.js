const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const KEYCLOAK_URL = import.meta.env.VITE_KEYCLOAK_URL || "http://localhost:8081";
const KEYCLOAK_REALM = import.meta.env.VITE_KEYCLOAK_REALM || "orderly";
const KEYCLOAK_CLIENT_ID = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || "orderly-frontend";
const TOKEN_KEY = "orderlyAccessToken";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function isAuthenticated() {
  return Boolean(getToken());
}

export function decodeToken(token = getToken()) {
  if (!token) return {};
  try {
    const payload = token.split(".")[1];
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(normalized));
  } catch {
    return {};
  }
}

export async function login(username, password) {
  const body = new URLSearchParams({
    client_id: KEYCLOAK_CLIENT_ID,
    grant_type: "password",
    username,
    password
  });

  const response = await fetch(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body
  });

  if (!response.ok) {
    throw new Error("Login failed.");
  }

  const data = await response.json();
  setToken(data.access_token);
  return data.access_token;
}

export async function registerAccount(payload) {
  return apiFetch("/api/auth/register", {
    method: "POST",
    body: payload,
    auth: false
  });
}

export async function ensureLocalUser() {
  const claims = decodeToken();
  const email = claims.email;
  const username = claims.preferred_username || claims.username || email?.split("@")[0];

  try {
    return await apiFetch("/api/users/me");
  } catch (error) {
    if (error.status !== 404) throw error;
  }

  return apiFetch("/api/users", {
    method: "POST",
    body: { email, username }
  });
}

export async function getCurrentUser() {
  return apiFetch("/api/users/me");
}

export async function listSpaces() {
  return apiFetch("/api/spaces");
}

export async function createSpace(payload) {
  return apiFetch("/api/spaces", { method: "POST", body: payload });
}

export async function deleteSpace(id) {
  return apiFetch(`/api/spaces/${id}`, { method: "DELETE" });
}

export async function getSpaceFull(id) {
  return apiFetch(`/api/spaces/${id}/full`);
}

export async function createBlock(spaceId, payload) {
  return apiFetch(`/api/spaces/${spaceId}/blocks`, { method: "POST", body: payload });
}

export async function updateBlock(blockId, payload) {
  return apiFetch(`/api/blocks/${blockId}`, { method: "PATCH", body: payload });
}

export async function deleteBlock(blockId) {
  return apiFetch(`/api/blocks/${blockId}`, { method: "DELETE" });
}

export async function createChecklistItem(blockId, payload) {
  return apiFetch(`/api/blocks/${blockId}/checklist-items`, { method: "POST", body: payload });
}

export async function updateChecklistItem(itemId, payload) {
  return apiFetch(`/api/checklist-items/${itemId}`, { method: "PATCH", body: payload });
}

export async function deleteChecklistItem(itemId) {
  return apiFetch(`/api/checklist-items/${itemId}`, { method: "DELETE" });
}

export async function createTableRow(blockId, payload) {
  return apiFetch(`/api/blocks/${blockId}/table-rows`, { method: "POST", body: payload });
}

export async function updateTableRow(rowId, payload) {
  return apiFetch(`/api/table-rows/${rowId}`, { method: "PATCH", body: payload });
}

export async function deleteTableRow(rowId) {
  return apiFetch(`/api/table-rows/${rowId}`, { method: "DELETE" });
}

export async function createDiagram(blockId, payload = { viewportX: 0, viewportY: 0, zoom: 1 }) {
  return apiFetch(`/api/blocks/${blockId}/diagram`, { method: "POST", body: payload });
}

export async function createDiagramNode(diagramId, payload) {
  return apiFetch(`/api/diagrams/${diagramId}/nodes`, { method: "POST", body: payload });
}

export async function updateDiagramNode(nodeId, payload) {
  return apiFetch(`/api/diagram-nodes/${nodeId}`, { method: "PATCH", body: payload });
}

export async function deleteDiagramNode(nodeId) {
  return apiFetch(`/api/diagram-nodes/${nodeId}`, { method: "DELETE" });
}

export async function createDiagramEdge(diagramId, payload) {
  return apiFetch(`/api/diagrams/${diagramId}/edges`, { method: "POST", body: payload });
}

export async function deleteDiagramEdge(edgeId) {
  return apiFetch(`/api/diagram-edges/${edgeId}`, { method: "DELETE" });
}

async function apiFetch(path, options = {}) {
  const { body, auth = true, ...rest } = options;
  const headers = {
    "Content-Type": "application/json",
    ...(rest.headers || {})
  };

  if (auth) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  });

  if (response.status === 204) return null;

  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json") ? await response.json() : await response.text();

  if (!response.ok) {
    const error = new Error(data?.message || "Request failed.");
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}
