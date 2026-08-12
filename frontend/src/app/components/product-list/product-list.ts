import { Component, OnInit, WritableSignal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { MediaService } from '../../services/media.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss'
})
export class ProductListComponent implements OnInit {
  products: WritableSignal<Product[]> = signal([]);
  loading = signal(true);
  error = signal('');

  constructor(
    private productService: ProductService,
    public mediaService: MediaService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: products => { this.products.set(products); this.loading.set(false); },
      error: () => { this.error.set('Failed to load products'); this.loading.set(false); }
    });
  }
}
