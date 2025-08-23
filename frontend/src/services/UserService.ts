import type { Page } from '../types';
import type { User } from '../types';

const BASE_API_URL = 'http://localhost:8080/digg/user';

export async function fetchUsers(page: number, size: number, query?: string): Promise<Page<User>> {
  const url = query && query.trim()
    ? `${BASE_API_URL}/${page}/${size}/search/${encodeURIComponent(query)}`
    : `${BASE_API_URL}/${page}/${size}`;

  const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
  if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
  return res.json();
}

export async function saveUser(user: User): Promise<User | undefined> {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
  const body = JSON.stringify(user);

  async function parseOptionalJson(res: Response) {
    const text = await res.text();
    if (!text) return undefined;
    try { return JSON.parse(text) as User; } catch { return undefined; }
  }

  async function tryPost(url: string) {
    if (import.meta.env.DEV) console.debug('[saveUser v3] POST', url, user);
    return fetch(url, { method: 'POST', headers, body });
  }

  // Single canonical endpoint first; then a few common fallbacks.
  const candidates = [
    BASE_API_URL,                  // POST /digg/user  ← preferred for both create and update
    `${BASE_API_URL}/save`,        // POST /digg/user/save
    `${BASE_API_URL}/update`,      // POST /digg/user/update
    `${BASE_API_URL}/edit`,        // POST /digg/user/edit
    `${BASE_API_URL}/add`,         // POST /digg/user/add
    `${BASE_API_URL}/create`,      // POST /digg/user/create
  ];

  let lastRes: Response | null = null;
  for (const url of candidates) {
    try {
      const res = await tryPost(url);
      lastRes = res;
      if (res.ok) {
        return await parseOptionalJson(res);
      }
      // Try next candidate on common "not supported" statuses
      if ([404, 405, 501].includes(res.status)) continue;
    } catch (e) {
      if (import.meta.env.DEV) console.warn('[saveUser v3] network error on', url, e);
      continue;
    }
  }

  if (lastRes) {
    const text = await lastRes.text().catch(() => '');
    throw new Error(`Failed to save user (HTTP ${lastRes.status})${text ? `: ${text}` : ''}`);
  } else {
    throw new Error('Failed to save user: no response from server.');
  }
}

export async function deleteUser(id: User['id']): Promise<boolean> {
  const url = `${BASE_API_URL}/${id}`;
  if (import.meta.env.DEV) console.debug('[deleteUser] DELETE', url);
  const res = await fetch(url, { method: 'DELETE', headers: { 'Accept': 'application/json' } });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`Failed to delete the user (HTTP ${res.status})${text ? `: ${text}` : ''}`);
  }
  return true;
}