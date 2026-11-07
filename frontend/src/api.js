const BASE = import.meta.env.VITE_API_URL || '/api';
const KEY = 'pt_user';

export function getStoredUser() {
  try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; }
}
export function storeUser(u) {
  try { u ? localStorage.setItem(KEY, JSON.stringify(u)) : localStorage.removeItem(KEY); } catch { /* ignore */ }
}

export class ApiError extends Error {
  constructor(message, status, errors) {
    super(message);
    this.status = status;
    this.errors = errors || {};
  }
}

/** Thin fetch wrapper: JSON in/out, auth header, normalised errors. */
export async function api(path, { method = 'GET', body, params } = {}) {
  const user = getStoredUser();
  const qs = params
    ? '?' + new URLSearchParams(Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined && v !== false)).toString()
    : '';
  let res;
  try {
    res = await fetch(`${BASE}${path}${qs}`, {
      method,
      headers: { 'Content-Type': 'application/json', ...(user ? { 'X-User-Email': user.email } : {}) },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError('Cannot reach the server. Is the backend running?', 0);
  }
  if (res.status === 204) return null;
  let data = null;
  try { data = await res.json(); } catch { /* no body */ }
  if (!res.ok) {
    if (res.status === 401 && user) { storeUser(null); window.dispatchEvent(new Event('pt-logout')); }
    throw new ApiError(data?.message || `Request failed (${res.status})`, res.status, data?.errors);
  }
  return data;
}
