export const AUTH_EXPIRED_EVENT = 'setu:auth-expired';

const BASE = (import.meta.env?.VITE_API_BASE_URL || '').replace(/\/+$/, '');

export function buildApiUrl(base, path) {
  const cleanBase = (base || '').replace(/\/+$/, '');
  if (!cleanBase || path.startsWith('http://') || path.startsWith('https://')) return path;
  if (path.startsWith(`${cleanBase}/`) || path === cleanBase) return path;
  if (cleanBase.endsWith('/api') && path.startsWith('/api/')) {
    return `${cleanBase.slice(0, -3).replace(/\/+$/, '')}${path}`;
  }
  return `${cleanBase}${path.startsWith('/') ? '' : '/'}${path}`;
}

function getToken() {
  return localStorage.getItem('sih_token') || '';
}

export function setToken(token) {
  if (token) localStorage.setItem('sih_token', token);
  else localStorage.removeItem('sih_token');
}

export async function api(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(buildApiUrl(BASE, path), {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch {
    data = null;
  }

  if (!res.ok) {
    if (res.status === 401) {
      setToken(null);
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT));
      }
    }
    const message = data && data.message ? data.message : `Request failed (${res.status})`;
    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

export const authApi = {
  login: (email, password) => api('/api/auth/login', { method: 'POST', body: { email, password } }),
  register: (payload) => api('/api/auth/register', { method: 'POST', body: payload }),
};

export const dashboardApi = {
  analytics: () => api('/api/dashboard/analytics'),
  me: () => api('/api/dashboard/me'),
};

export const problemApi = {
  list: (params = {}) => {
    const qs = new URLSearchParams(Object.entries(params).filter(([, v]) => v)).toString();
    return api(`/api/problems${qs ? `?${qs}` : ''}`);
  },
  get: (id) => api(`/api/problems/${id}`),
  create: (payload) => api('/api/problems', { method: 'POST', body: payload }),
  analyze: (id) => api(`/api/problems/${id}/analyze`, { method: 'POST' }),
  verify: (id) => api(`/api/problems/${id}/verify`, { method: 'POST' }),
  capabilities: (id) => api(`/api/problems/${id}/capabilities`),
};

export const matchingApi = {
  run: (id) => api(`/api/problems/${id}/match`, { method: 'POST' }),
  results: (id, type = '') => api(`/api/problems/${id}/matches${type ? `?type=${type}` : ''}`),
  recommend: (id) => api(`/api/problems/${id}/recommend`, { method: 'POST' }),
  recovery: (id, missingCapability) =>
    api(`/api/problems/${id}/recovery-recommendations?missingCapability=${encodeURIComponent(missingCapability)}`),
};

export const projectApi = {
  list: () => api('/api/projects'),
  get: (id) => api(`/api/projects/${id}`),
  create: (payload) => api('/api/projects', { method: 'POST', body: payload }),
  changeStatus: (id, body) => api(`/api/projects/${id}/status`, { method: 'PUT', body }),
  progress: (id, progress) => api(`/api/projects/${id}/progress`, { method: 'PUT', body: { progress } }),
  addTask: (id, body) => api(`/api/projects/${id}/tasks`, { method: 'POST', body }),
  updateTask: (id, taskId, body) => api(`/api/projects/${id}/tasks/${taskId}`, { method: 'PUT', body }),
  addMilestone: (id, body) => api(`/api/projects/${id}/milestones`, { method: 'POST', body }),
  updateMilestone: (id, milestoneId, body) => api(`/api/projects/${id}/milestones/${milestoneId}`, { method: 'PUT', body }),
  addComment: (id, body) => api(`/api/projects/${id}/comments`, { method: 'POST', body }),
  addDocument: (id, body) => api(`/api/projects/${id}/documents`, { method: 'POST', body }),
  reportFailure: (id, body) => api(`/api/projects/${id}/failure`, { method: 'POST', body }),
  resolveFailure: (id) => api(`/api/projects/${id}/failure/resolve`, { method: 'POST' }),
  recovery: (id) => api(`/api/projects/${id}/recovery-recommendations`),
};

export const impactApi = {
  list: () => api('/api/impact'),
  stats: () => api('/api/impact/stats'),
  forProject: (id) => api(`/api/projects/${id}/impact`),
  submit: (id, body) => api(`/api/projects/${id}/impact`, { method: 'POST', body }),
  addEvidence: (id, body) => api(`/api/projects/${id}/impact/evidence`, { method: 'POST', body }),
  reviewEvidence: (evidenceId, status) =>
    api(`/api/impact/evidence/${evidenceId}/review`, { method: 'PUT', body: { status } }),
  allEvidence: () => api('/api/impact/evidence'),
};

export const notificationApi = {
  list: () => api('/api/notifications'),
  unreadCount: () => api('/api/notifications/unread-count'),
  markRead: (id) => api(`/api/notifications/${id}/read`, { method: 'POST' }),
};

export const catalogApi = {
  universities: () => api('/api/universities'),
  teams: () => api('/api/student-teams'),
  faculty: () => api('/api/faculty'),
  industries: () => api('/api/industries'),
};