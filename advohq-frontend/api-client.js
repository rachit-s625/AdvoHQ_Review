// AdvoHQ shared API client — talks to the Java backend (advohq-backend).
// Auth: reads the JWT that login.html / create-account.html store after
// a successful login/register.
const ADVOHQ_API_BASE = 'https://advohq-backend.onrender.com';

function advohqAuthHeaders() {
  const token = localStorage.getItem('advohq_token');
  return token ? { authorization: `Bearer ${token}` } : {};
}

async function advohqApiFetch(path, options = {}) {
  const res = await fetch(`${ADVOHQ_API_BASE}${path}`, {
    ...options,
    headers: {
      'content-type': 'application/json',
      ...advohqAuthHeaders(),
      ...(options.headers || {}),
    },
  });
  if (res.status === 401) {
    localStorage.removeItem('advohq_token');
    localStorage.removeItem('advohq_user');
    window.location.href = 'login.html';
    throw new Error('Not authenticated');
  }
  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.message || `Request failed (${res.status})`);
  }
  return res.status === 204 ? null : res.json();
}

// File uploads need multipart/form-data (browser sets the boundary itself),
// so they can't go through advohqApiFetch's json content-type header.
async function advohqApiUpload(path, formData) {
  const res = await fetch(`${ADVOHQ_API_BASE}${path}`, {
    method: 'POST',
    headers: { ...advohqAuthHeaders() },
    body: formData,
  });
  if (res.status === 401) {
    localStorage.removeItem('advohq_token');
    localStorage.removeItem('advohq_user');
    window.location.href = 'login.html';
    throw new Error('Not authenticated');
  }
  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.message || `Upload failed (${res.status})`);
  }
  return res.json();
}

window.API = {
  // ── Schedule events ──
  getEvents(from, to) {
    const qs = new URLSearchParams();
    if (from) qs.set('from', from);
    if (to) qs.set('to', to);
    const suffix = qs.toString() ? `?${qs}` : '';
    return advohqApiFetch(`/api/events${suffix}`);
  },
  createEvent: (payload) => advohqApiFetch('/api/events', { method: 'POST', body: JSON.stringify(payload) }),
  updateEvent: (id, payload) => advohqApiFetch(`/api/events/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteEvent: (id) => advohqApiFetch(`/api/events/${id}`, { method: 'DELETE' }),

  // ── Cases (the Library's "folders") ──
  getCases: () => advohqApiFetch('/api/cases'),
  createCase: (payload) => advohqApiFetch('/api/cases', { method: 'POST', body: JSON.stringify(payload) }),
  updateCase: (id, payload) => advohqApiFetch(`/api/cases/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteCase: (id) => advohqApiFetch(`/api/cases/${id}`, { method: 'DELETE' }),

  // ── Documents (the Library's "files"; actual bytes live in S3) ──
  getDocuments: (caseId) => advohqApiFetch(`/api/documents${caseId ? `?caseId=${caseId}` : ''}`),
  uploadDocument(file, caseId) {
    const fd = new FormData();
    fd.append('file', file);
    if (caseId) fd.append('caseId', caseId);
    return advohqApiUpload('/api/documents', fd);
  },
  getDocumentDownloadUrl: (id) => advohqApiFetch(`/api/documents/${id}/download-url`),
  // Inline disposition: the browser renders the file (PDF viewer, image)
  // instead of downloading it.
  getDocumentViewUrl: (id) => advohqApiFetch(`/api/documents/${id}/download-url?disposition=inline`),
  // Trash: move to trash (recoverable), restore, or delete permanently.
  getTrash: () => advohqApiFetch('/api/documents/trash'),
  trashDocument: (id) => advohqApiFetch(`/api/documents/${id}/trash`, { method: 'POST' }),
  restoreDocument: (id) => advohqApiFetch(`/api/documents/${id}/restore`, { method: 'POST' }),
  deleteDocument: (id) => advohqApiFetch(`/api/documents/${id}`, { method: 'DELETE' }),

  // ── Profile / settings / custom case stages ──
  getProfile: () => advohqApiFetch('/api/me'),
  updateProfile: (payload) => advohqApiFetch('/api/me', { method: 'PUT', body: JSON.stringify(payload) }),
  // Returns a fresh AuthResponse — the backend invalidates all older tokens.
  changePassword: (currentPassword, newPassword) =>
    advohqApiFetch('/api/me/password', { method: 'PUT', body: JSON.stringify({ currentPassword, newPassword }) }),
  deleteAccount: () => advohqApiFetch('/api/me', { method: 'DELETE' }),
  getSettings: () => advohqApiFetch('/api/settings'),
  upsertSetting: (key, value) => advohqApiFetch('/api/settings', { method: 'PUT', body: JSON.stringify({ key, value }) }),
  getStages: () => advohqApiFetch('/api/stages'),
  addStage: (name) => advohqApiFetch('/api/stages', { method: 'POST', body: JSON.stringify({ name }) }),
  deleteStage: (id) => advohqApiFetch(`/api/stages/${id}`, { method: 'DELETE' }),

  // ── Notifications ──
  // No backend endpoint exists for these yet, so these stay stubs (same
  // "all caught up" behavior every page already falls back to when
  // api-client.js was missing).
  getNotifications:         async () => ({ notifications: [], unread_count: 0 }),
  getNotificationCount:     async () => ({ unread_count: 0 }),
  markNotificationRead:     async () => {},
  markAllNotificationsRead: async () => {},
};
