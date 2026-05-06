import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateProductRequest, Product, UpdateProductRequest } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly base = '/api/products';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.base);
  }

  getById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.base}/${id}`);
  }

  create(req: CreateProductRequest): Observable<Product> {
    return this.http.post<Product>(this.base, req);
  }

  update(id: string, req: UpdateProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.base}/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  attachImages(id: string, imageIds: string[]): Observable<Product> {
    return this.http.put<Product>(`${this.base}/${id}/images`, { imageIds });
  }
}
