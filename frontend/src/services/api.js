const DEFAULT_BASE_URL = 'http://localhost:8080/adp-assignment';

export function buildApiUrl(path) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || DEFAULT_BASE_URL;
  return `${baseUrl.replace(/\/$/, '')}${path}`;
}

export async function requestJson(path, options = {}) {
  const response = await fetch(buildApiUrl(path), {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || 'Request failed');
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }

  return response.text();
}
