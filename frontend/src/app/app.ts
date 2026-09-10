import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NotificationService } from './services/notification.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    <router-outlet />
    @if (notification.message()) {
      <div class="app-notice" [class.error]="notification.kind() === 'error'" role="alert">
        <span>{{ notification.message() }}</span>
        <button type="button" (click)="notification.clear()" aria-label="Dismiss notification">×</button>
        <span class="notice-timer" aria-hidden="true"></span>
      </div>
    }
  `,
  styles: [`
    .app-notice { position: fixed; z-index: 20; right: 1.25rem; bottom: 1.25rem; display: flex; align-items: center; gap: 1rem; overflow: hidden; max-width: min( calc(100vw - 2.5rem), 380px); padding: .9rem 1rem 1.05rem; border-left: 4px solid #e6b84c; border-radius: 4px; background: #123b52; color: #fff; box-shadow: 0 14px 35px rgba(13,41,56,.22); font: 500 .85rem 'DM Sans', sans-serif; }
    .app-notice button { border: 0; background: transparent; color: #fff; cursor: pointer; font-size: 1.25rem; line-height: 1; }
    .notice-timer { position: absolute; right: 0; bottom: 0; left: 0; height: 3px; transform-origin: left; background: #e6b84c; animation: notification-countdown 5s linear forwards; }
    @keyframes notification-countdown { from { transform: scaleX(1); } to { transform: scaleX(0); } }
    @media (prefers-reduced-motion: reduce) { .notice-timer { animation: none; } }
  `]
})
export class App {
  constructor(public notification: NotificationService) {}
}
