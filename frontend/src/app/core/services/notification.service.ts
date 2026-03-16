import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  get supported(): boolean {
    return typeof window !== 'undefined' && 'Notification' in window;
  }

  get permission(): NotificationPermission | 'unsupported' {
    if (!this.supported) {
      return 'unsupported';
    }
    return Notification.permission;
  }

  async requestPermission(): Promise<NotificationPermission | 'unsupported'> {
    if (!this.supported) {
      return 'unsupported';
    }

    if (Notification.permission === 'granted') {
      return Notification.permission;
    }

    return Notification.requestPermission();
  }

  notify(title: string, options?: NotificationOptions): boolean {
    if (!this.supported || Notification.permission !== 'granted') {
      return false;
    }

    new Notification(title, options);
    return true;
  }
}