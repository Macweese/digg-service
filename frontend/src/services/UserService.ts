import type { Page } from '../types';
import type { User } from '../types';

const BASE_API_URL = 'http://localhost:8080/digg/user';

export async function fetchUsers(page: number, size: number, query?: string): Promise<Page<User>> {
  const url = query && query.trim()
    ? `${BASE_API_URL}/${page}/${size}/search/${encodeURIComponent(query)}`
    : `${BASE_API_URL}/${page}/${size}`;

  const res = await fetch(url);
  if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
  return res.json();
}

export async function saveUser(user: User): Promise<User | undefined> {
  const isEditing = !!user.id;
  const url = isEditing ? `${BASE_API_URL}/${user.id}` : BASE_API_URL;
  const method = isEditing ? 'PUT' : 'POST';
  const res = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user),
  });
  if (!res.ok) throw new Error('Failed to save the user.');
  try {
    return await res.json() as User;
  } catch {
    return undefined; // some APIs return 204 No Content
  }
}

export async function deleteUser(id: User['id']): Promise<boolean> {
  const res = await fetch(`${BASE_API_URL}/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Failed to delete the user.');
  return true;
}