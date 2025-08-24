type SubscriptionCallback = (message: { body: string }) => void;

let lastClient: Client | null = null;

class Client {
  brokerURL?: string;
  debug?: (msg: string) => void;
  onConnect?: (frame?: unknown) => void;
  onStompError?: (frame?: unknown) => void;

  private subscriptions = new Map<string, SubscriptionCallback>();
  private connected = false;

  constructor(config: Partial<Client> = {}) {
    Object.assign(this, config);
    lastClient = this;
  }

  activate() {
    this.connected = true;
    this.debug?.('mock: activate');
    this.onConnect?.({});
  }

  deactivate() {
    this.connected = false;
    this.debug?.('mock: deactivate');
  }

  subscribe(destination: string, callback: SubscriptionCallback) {
    this.subscriptions.set(destination, callback);
    return {
      unsubscribe: () => this.subscriptions.delete(destination),
    };
  }

  publish({ destination, body }: { destination: string; body: string }) {
    const cb = this.subscriptions.get(destination);
    if (cb) cb({ body });
  }

  // Test helper: trigger a message on a destination
  __emit(destination: string, payload: unknown) {
    const cb = this.subscriptions.get(destination);
    if (!cb) return;
    const body = typeof payload === 'string' ? payload : JSON.stringify(payload);
    cb({ body });
  }
}

function __getLastClient() {
  return lastClient;
}

export { Client, __getLastClient };