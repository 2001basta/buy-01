import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const notification = inject(NotificationService);

  const token = auth.getToken();
  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const serverMessage = typeof error.error?.message === 'string'
        ? error.error.message
        : typeof error.error?.error === 'string'
          ? error.error.error
          : null;
      let message = serverMessage ?? 'An unexpected error occurred.';

      switch (error.status) {
        case 0:
          message = 'Cannot reach the server. Check your connection.';
          break;
        case 400:
          message = serverMessage ?? 'Please check the submitted information.';
          break;
        case 401:
          message = serverMessage ?? 'Your session has expired. Please log in again.';
          localStorage.removeItem('accessToken');
          router.navigate(['/auth/login']);
          break;
        case 403:
          message = serverMessage ?? 'You are not allowed to perform this action.';
          break;
        case 404:
          message = serverMessage ?? 'The requested resource was not found.';
          break;
        case 413:
          message = serverMessage ?? 'The selected file is too large.';
          break;
        case 500:
          message = 'The server encountered a problem. Please try again.';
          break;
      }

      notification.show(message);
      return throwError(() => error);
    })
  );
};
