import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserProfile, ProfileUpdateRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly base = '/api/users/me';

  constructor(private http: HttpClient) {}

  get(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.base);
  }

  update(request: ProfileUpdateRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(this.base, request);
  }
}
