import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MediaResponse {
  id: string;
  url: string;
  mimeType: string;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class MediaService {
  private readonly base = '/api/media';

  constructor(private http: HttpClient) {}

  upload(file: File, productId?: string): Observable<MediaResponse> {
    const form = new FormData();
    form.append('file', file);
    if (productId) form.append('productId', productId);
    return this.http.post<MediaResponse>(`${this.base}/upload`, form);
  }

  getUrl(id: string): string {
    return `${this.base}/${id}`;
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
