import type {Page, User} from '../types';

const BASE_API_URL = 'http://localhost:8080/digg/user';
const SAVE_ENDPOINT_CACHE_KEY = 'digg.user.saveEndpoint';

// Candidate endpoints to probe in order (first that works is cached)
const SAVE_ENDPOINT_CANDIDATES = [
    BASE_API_URL,                  // POST /digg/user
    `${BASE_API_URL}/save`,        // POST /digg/user/save
    `${BASE_API_URL}/update`,      // POST /digg/user/update
    `${BASE_API_URL}/edit`,        // POST /digg/user/edit
    `${BASE_API_URL}/add`,         // POST /digg/user/add
    `${BASE_API_URL}/create`,      // POST /digg/user/create
];

function getConfiguredSaveEndpoint(): string | null {
    // Allow hardcoding via env var (vite)
    const fromEnv = (import.meta as unknown as { env?: { VITE_USER_SAVE_ENDPOINT?: string } }).env?.VITE_USER_SAVE_ENDPOINT;
    if (fromEnv && typeof fromEnv === 'string' && fromEnv.trim()) return fromEnv.trim();

    // Then check cache
    try {
        const cached = localStorage.getItem(SAVE_ENDPOINT_CACHE_KEY);
        if (cached && cached.startsWith('http')) return cached;
    } catch {
        // ignore storage errors
    }
    return null;
}

function cacheSaveEndpoint(url: string) {
    try {
        localStorage.setItem(SAVE_ENDPOINT_CACHE_KEY, url);
    } catch {
        // ignore storage errors
    }
}

function clearCachedSaveEndpoint() {
    try {
        localStorage.removeItem(SAVE_ENDPOINT_CACHE_KEY);
    } catch {
        // ignore
    }
}

export async function fetchUsers(page: number, size: number, query?: string): Promise<Page<User>> {
    const url = query && query.trim()
        ? `${BASE_API_URL}/${page}/${size}/search/${encodeURIComponent(query)}`
        : `${BASE_API_URL}/${page}/${size}`;

    const res = await fetch(url, {headers: {Accept: 'application/json'}});
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    return res.json();
}

export async function saveUser(user: User): Promise<User | undefined> {
    const headers = {
        'Content-Type': 'application/json',
        Accept: 'application/json',
    };
    const body = JSON.stringify(user);

    async function parseOptionalJson(res: Response) {
        const text = await res.text();
        if (!text) return undefined;
        try {
            return JSON.parse(text) as User;
        } catch {
            return undefined;
        }
    }

    async function postTo(url: string) {
        if (import.meta.env.DEV) console.debug('[saveUser] POST', url);
        return fetch(url, {method: 'POST', headers, body});
    }

    // 1) If we have a configured/cached endpoint, try it first
    const configured = getConfiguredSaveEndpoint();
    if (configured) {
        const res = await postTo(configured);
        if (res.ok) return parseOptionalJson(res);

        // If it stopped working, clear and fall back to discovery
        if ([404, 405, 501].includes(res.status)) {
            clearCachedSaveEndpoint();
        } else {
            const text = await res.text().catch(() => '');
            throw new Error(`Failed to save user (HTTP ${res.status})${text ? `: ${text}` : ''}`);
        }
    }

    // 2) Discover: try candidates until one succeeds, then cache it
    let lastRes: Response | null = null;
    for (const url of SAVE_ENDPOINT_CANDIDATES) {
        const res = await postTo(url);
        lastRes = res;
        if (res.ok) {
            cacheSaveEndpoint(url);
            return parseOptionalJson(res);
        }
        // Only keep probing on "not supported" style statuses
        if (![404, 405, 501].includes(res.status)) {
            const text = await res.text().catch(() => '');
            throw new Error(`Failed to save user (HTTP ${res.status})${text ? `: ${text}` : ''}`);
        }
    }

    // 3) All candidates failed
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
    const res = await fetch(url, {method: 'DELETE', headers: {Accept: 'application/json'}});
    if (!res.ok) {
        const text = await res.text().catch(() => '');
        throw new Error(`Failed to delete the user (HTTP ${res.status})${text ? `: ${text}` : ''}`);
    }
    return true;
}