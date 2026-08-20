import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

class WebSocketService {
  private client: Client | null = null;

  public connect(onNotificationReceived: (notification: any) => void) {
    const token = localStorage.getItem('rrr_token');

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : '',
      },
      debug: () => {},
      reconnectDelay: 5000,
      onConnect: () => {
        // Subscribe to general SOS topic
        this.client?.subscribe('/topic/sos-alerts', (message) => {
          if (message.body) {
            onNotificationReceived(JSON.parse(message.body));
          }
        });

        // Subscribe to user-specific notifications
        this.client?.subscribe('/user/queue/notifications', (message) => {
          if (message.body) {
            onNotificationReceived(JSON.parse(message.body));
          }
        });
      },
    });

    this.client.activate();
  }

  public disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }
}

export const webSocketService = new WebSocketService();
