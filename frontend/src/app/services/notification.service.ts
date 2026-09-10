import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly durationMs = 5000;
  private dismissTimer: ReturnType<typeof setTimeout> | null = null;
  readonly message = signal('');
  readonly kind = signal<'info' | 'error' | 'success'>('info');

  show(message: string, kind: 'info' | 'error' | 'success' = 'error'): void {
    this.clearTimer();
    this.message.set(message);
    this.kind.set(kind);
    this.dismissTimer = setTimeout(() => this.clear(), this.durationMs);
  }

  clear(): void {
    this.clearTimer();
    this.message.set('');
  }

  private clearTimer(): void {
    if (this.dismissTimer) {
      clearTimeout(this.dismissTimer);
      this.dismissTimer = null;
    }
  }
}
