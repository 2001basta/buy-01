import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { AuthResponse, LoginRequest, SignupRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'accessToken';

  constructor(private http: HttpClient, private router: Router) {}

  signup(req: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/signup', req).pipe(
      tap(res => this.storeToken(res.accessToken))
    );
  }

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', req).pipe(
      tap(res => this.storeToken(res.accessToken))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    const payload = this.getPayload();
    return !!payload && typeof payload['exp'] === 'number' && payload['exp'] * 1000 > Date.now();
  }

  getRole(): string | null {
    const roles = this.getPayload()?.['roles'];
    return Array.isArray(roles) ? roles[0] ?? null : typeof roles === 'string' ? roles : null;
  }

  getUserId(): string | null {
    const subject = this.getPayload()?.['sub'];
    return typeof subject === 'string' ? subject : null;
  }

  isSeller(): boolean {
    return this.getRole() === 'SELLER';
  }

  private storeToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private getPayload(): Record<string, any> | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const encoded = token.split('.')[1];
      const normalized = encoded.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')));
    } catch {
      return null;
    }
  }
}
