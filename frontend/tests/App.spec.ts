// @vitest-environment jsdom

import {flushPromises, mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {nextTick} from 'vue';
import App from '../src/App.vue';

// Mock SockJS to prevent real network calls
vi.mock('sockjs-client', () => {
    return {
        default: vi.fn().mockImplementation(() => {
            return {close: vi.fn()};
        }),
    };
});

// Keep a handle to the last created STOMP client so tests can emit messages
let lastClient: any = null;

// Mock STOMP Client
vi.mock('@stomp/stompjs', () => {
    class Client {
        webSocketFactory?: () => any;
        reconnectDelay?: number;
        onConnect?: (...args: any[]) => void;
        onStompError?: (...args: any[]) => void;
        subscriptions: Record<string, Function> = {};

        constructor(opts: any) {
            this.webSocketFactory = opts.webSocketFactory;
            this.reconnectDelay = opts.reconnectDelay;
            this.onConnect = opts.onConnect;
            this.onStompError = opts.onStompError;
            lastClient = this;
        }

        activate() {
            // Immediately simulate a successful connection
            this.onConnect && this.onConnect();
        }

        subscribe(destination: string, cb: Function) {
            this.subscriptions[destination] = cb;
            return {
                id: 'sub', unsubscribe: () => {
                }
            };
        }

        // Helper so the test can simulate an incoming message
        __emit(destination: string, bodyObj: any) {
            const cb = this.subscriptions[destination];
            if (cb) cb({body: JSON.stringify(bodyObj)});
        }
    }

    return {Client, __getLastClient: () => lastClient};
});

// Reusable page response matching your App.vue expectations
const pageResponse = {
    content: [
        {id: 1, name: 'Alice', email: 'alice@example.com', telephone: '123', address: 'Main St'},
    ],
    totalPages: 1,
    totalElements: 1,
    number: 0,
};

describe('App.vue', () => {
    beforeEach(() => {
        // Mock fetch for GET and DELETE used in App.vue
        // @ts-ignore
        global.fetch = vi.fn((url: string, options?: any) => {
            if (options && options.method === 'DELETE') {
                return Promise.resolve({ok: true, json: async () => ({})});
            }
            // default GET for users page
            return Promise.resolve({ok: true, json: async () => pageResponse});
        });
    });

    it('loads users on mount and renders a row', async () => {
        const wrapper = mount(App);
        await flushPromises();

        // Should have called fetch at least once for initial load
        expect(global.fetch).toHaveBeenCalledTimes(1);

        // Renders table row with user.name
        expect(wrapper.html()).toContain('Alice');
    });

    it('reloads users when a WebSocket event arrives', async () => {
        const {__getLastClient} = await import('@stomp/stompjs');

        const wrapper = mount(App);
        await flushPromises();

        const client: any = __getLastClient();
        expect(client).toBeTruthy();

        // Simulate server event message that your frontend expects
        client.__emit('/topic/users', {event: 'USER_DELETED'});

        await flushPromises();

        // Should have fetched again (initial + reload)
        expect(global.fetch).toHaveBeenCalledTimes(2);

        // Still shows a row (from mocked response)
        expect(wrapper.html()).toContain('Alice');
    });

    it('opens delete confirm and performs DELETE then refreshes', async () => {
        // Fallback for apps that use native window.confirm instead of a modal
        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

        const wrapper = mount(App, {
            attachTo: document.body,
            global: {stubs: {teleport: true, transition: true}},
        });
        await flushPromises();

        // Click the delete button in the first table row (second action button)
        const firstRow = wrapper.findAll('tbody tr')[0];
        expect(firstRow).toBeTruthy();
        const actionButtons = firstRow.findAll('button');
        expect(actionButtons.length).toBeGreaterThan(1);

        await actionButtons[1].trigger('click');
        await nextTick();
        await flushPromises();
        await new Promise(r => setTimeout(r, 0));
        await flushPromises();

        // Try to find a confirm button in a modal (various common labels)
        const btns = wrapper.findAll('button');
        const confirmBtn =
            btns.find(b => /confirm\s*delete/i.test(b.text())) ||
            btns.find(b => /^\s*confirm\s*$/i.test(b.text())) ||
            btns.find(b => /^\s*delete\s*$/i.test(b.text())) ||
            btns.find(b => /^\s*yes\b/i.test(b.text())) ||
            null;

        if (confirmBtn) {
            await confirmBtn.trigger('click');
            await flushPromises();
        }

        // Expect DELETE call and then a reload (GET)
        // Total calls: initial GET + DELETE + reload GET => 3
        const calls = (global.fetch as any).mock.calls;
        const deleteCall = calls.find((args: any[]) => args[1]?.method === 'DELETE');

        // If we didn't find a modal confirm button, ensure native confirm was used
        if (!confirmBtn) {
            expect(confirmSpy).toHaveBeenCalled();
        }

        expect(deleteCall).toBeTruthy();
        expect(global.fetch).toHaveBeenCalledTimes(3);

        confirmSpy.mockRestore();
    });
});