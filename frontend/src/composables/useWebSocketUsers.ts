import { onMounted, onBeforeUnmount } from 'vue';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { UserEventType } from '../events';

export function useWebSocketUsers(onUsersChanged: () => Promise<void> | void) {
  let stompClient: Client | null = null;

  function connect(): void {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 60000,
      onConnect: () => {
        stompClient?.subscribe('/topic/users', async (message) => {
          let event: string | undefined;
          try {
            const payload = JSON.parse(message.body);
            event = typeof payload === 'string' ? payload : payload?.event;
          } catch {
            event = message.body as string;
          }
          if (event && Object.values<string>(UserEventType as any).includes(event)) {
            await onUsersChanged();
          }
        });
      },
      onStompError: () => {
        // Optionally handle/log
      }
    });
    stompClient.activate();
  }

  function disconnect(): void {
    try { stompClient?.deactivate(); } catch {}
    stompClient = null;
  }

  onMounted(connect);
  onBeforeUnmount(disconnect);

  return { connect, disconnect };
}