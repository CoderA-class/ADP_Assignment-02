/*
 * api.js – every call the frontend makes to the Spring Boot backend.
 *
 * By default requests go to "/adp-assignment/..." on the SAME origin:
 *   - in development the Vite dev server proxies them to http://localhost:8080
 *   - in production Spring Boot serves the built app itself
 * so there are no CORS issues and it doesn't matter which one you start first.
 * Set VITE_API_BASE_URL (e.g. http://localhost:8080/adp-assignment) to override.
 */
const BASE_URL = (import.meta.env?.VITE_API_BASE_URL || '/adp-assignment').replace(/\/$/, '');

export class ApiError extends Error {
  constructor(message, status, offline = false) {
    super(message);
    this.status = status;
    this.offline = offline;
  }
}

/* Spring Security puts its CSRF token in the XSRF-TOKEN cookie; we send it back in a header */
function csrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

/* Any 401 from the server means the login session has ended */
function sessionExpired() {
  window.dispatchEvent(new Event('auth-expired'));
}

export async function request(path, { method = 'GET', body, query, form, retried = false } = {}) {
  let url = BASE_URL + path;
  const unsafe = method !== 'GET' && method !== 'HEAD';
  // Make sure we have a CSRF token before the first POST/PUT/DELETE
  if (unsafe && !csrfToken() && path !== '/health') {
    try { await fetch(`${BASE_URL}/health`, { credentials: 'include' }); } catch { /* handled below */ }
  }
  if (query) {
    const params = new URLSearchParams();
    Object.entries(query).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') params.append(k, v);
    });
    const qs = params.toString();
    if (qs) url += `?${qs}`;
  }

  const headers = {};
  let payload;
  if (form) {
    headers['Content-Type'] = 'application/x-www-form-urlencoded';
    payload = new URLSearchParams(form).toString();
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }
  if (unsafe && csrfToken()) headers['X-XSRF-TOKEN'] = csrfToken();

  let response;
  try {
    response = await fetch(url, { method, headers, body: payload, credentials: 'include' });
  } catch {
    throw new ApiError('Cannot reach the server. Is the backend running?', 0, true);
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }

  if (!response.ok) {
    // The CSRF token rotates after login/logout – fetch a fresh one and try once more
    if (response.status === 403 && data && data.code === 'CSRF' && !retried) {
      try { await fetch(`${BASE_URL}/health`, { credentials: 'include' }); } catch { /* ignore */ }
      return request(path, { method, body, query, form, retried: true });
    }
    if (response.status === 401 && path !== '/auth/login' && path !== '/auth/me') sessionExpired();
    const offline = response.status === 502 || response.status === 503 || response.status === 504 || (data && data.offline);
    const message = (data && typeof data === 'object' && data.message)
      || (offline ? 'Cannot reach the server. Is the backend running?' : null)
      || (response.status === 404 ? 'Not found.' : `Request failed (${response.status}).`);
    throw new ApiError(message, response.status, offline);
  }
  return data;
}

/* Builds the usual CRUD calls for an entity */
function crud(base) {
  return {
    all: () => request(`${base}/all`),
    read: (id) => request(`${base}/read/${encodeURIComponent(id)}`),
    create: (entity) => request(`${base}/create`, { method: 'POST', body: entity }),
    update: (entity) => request(`${base}/update`, { method: 'PUT', body: entity }),
    remove: (id) => request(`${base}/delete/${encodeURIComponent(id)}`, { method: 'DELETE' }),
    search: (name) => request(`${base}/search`, { query: { name } })
  };
}

export const api = {
  health: () => request('/health'),

  auth: {
    // Spring Security form login (Blackboard notes: FORM BASED authentication)
    login: (email, password, rememberMe) => request('/auth/login', {
      method: 'POST', form: { email, password, ...(rememberMe ? { 'remember-me': 'on' } : {}) }
    }),
    logout: () => request('/auth/logout', { method: 'POST' }),
    me: () => request('/auth/me'),
    updateMe: (changes) => request('/auth/me', { method: 'PUT', body: changes }),
    changePassword: (currentPassword, newPassword) =>
      request('/auth/change-password', { method: 'POST', body: { currentPassword, newPassword } })
  },

  customers: {
    ...crud('/customers'),
    orders: (id) => request(`/customers/${encodeURIComponent(id)}/orders`)
  },
  employees: crud('/employees'),
  suppliers: crud('/suppliers'),
  products: {
    ...crud('/products'),
    restock: (id, amount) => request(`/products/${encodeURIComponent(id)}/restock`, { method: 'PUT', query: { amount } })
  },
  orders: {
    ...crud('/orders'),
    checkout: (payload) => request('/orders/checkout', { method: 'POST', body: payload })
  },

  leave: {
    all: () => request('/leave/all'),
    forEmployee: (id) => request(`/leave/employee/${encodeURIComponent(id)}`),
    request: (leave) => request('/leave/request', { method: 'POST', body: leave }),
    approve: (id, reviewerId, comment) => request(`/leave/${id}/approve`, { method: 'PUT', body: { reviewerId, comment } }),
    decline: (id, reviewerId, comment) => request(`/leave/${id}/decline`, { method: 'PUT', body: { reviewerId, comment } }),
    cancel: (id) => request(`/leave/${id}/cancel`, { method: 'PUT' })
  },

  attendance: {
    all: () => request('/attendance/all'),
    forEmployee: (id) => request(`/attendance/employee/${encodeURIComponent(id)}`),
    current: (id) => request(`/attendance/current/${encodeURIComponent(id)}`),
    clockIn: (id) => request(`/attendance/clock-in/${encodeURIComponent(id)}`, { method: 'POST' }),
    clockOut: (id) => request(`/attendance/clock-out/${encodeURIComponent(id)}`, { method: 'POST' })
  },

  notifications: {
    // the server works out whose notifications to return from the login session
    list: () => request('/notifications'),
    markRead: (id) => request(`/notifications/${id}/read`, { method: 'PUT' }),
    markAllRead: () => request('/notifications/read-all', { method: 'PUT' }),
    runChecks: () => request('/notifications/run-checks', { method: 'POST' })
  }
};
