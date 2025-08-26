import {onBeforeUnmount, onMounted} from 'vue';
import {Client, type IMessage} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {UserEventType} from '../events';

export function useWebSocketUsers(onUsersChanged: () => Promise<void> | void) {
    let stompClient: Client | null = null;

    const ALLOWED_EVENTS = new Set<string>(Object.values(UserEventType as any));

    function extractEvent(body: string): string | null {
        // Try JSON first
        try {
            const json = JSON.parse(body);

            // Common key names that servers use
            const candidates = [
                json?.event,
                json?.type,
                json?.eventType,
                json?.action,
                json?.name,
                json?.payload?.event,
                json?.payload?.type,
                json?.data?.event,
                json?.data?.type,
            ].filter((v) => typeof v === 'string') as string[];

            if (candidates.length > 0) return candidates[0];
        } catch {
            // Not JSON, might be a plain string event
            if (typeof body === 'string' && body.trim().length > 0) {
                return body.trim();
            }
        }
        return null;
    }

    function connect(): void {
        stompClient = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws') as any,
            reconnectDelay: 5000,
            onConnect: () => {
                stompClient?.subscribe('/topic/users', async (message: IMessage) => {
                    const event = extractEvent(message.body);
                    if (import.meta.env.DEV) {
                        // Debug in dev only
                        console.debug('[WS] /topic/users message:', {raw: message.body, event});
                    }

                    // If event matches our known events OR we’re unsure, reload.
                    // Safer default: reload on any message on this topic.
                    if (!event || ALLOWED_EVENTS.has(event)) {
                        await onUsersChanged();
                    } else {
                        // If server uses a different enum, still reload to keep clients in sync.
                        await onUsersChanged();
                    }
                });
            },
            onStompError: (frame) => {
                if (import.meta.env.DEV) {
                    console.warn('[WS] STOMP error:', frame.headers['message'], frame.body);
                }
            },
            onWebSocketError: (ev) => {
                if (import.meta.env.DEV) {
                    console.warn('[WS] socket error:', ev);
                }
            },
            debug: (msg: string) => {
                if (import.meta.env.DEV) console.debug('[STOMP]', msg);
            },
        });

        stompClient.activate();
    }

    function disconnect(): void {
        try {
            stompClient?.deactivate();
        } catch {
            // ignore
        }
        stompClient = null;
    }

    onMounted(connect);
    onBeforeUnmount(disconnect);

    return {connect, disconnect};
}