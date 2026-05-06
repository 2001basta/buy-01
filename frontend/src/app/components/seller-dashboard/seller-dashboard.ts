import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { MediaService } from '../../services/media.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-seller-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './seller-dashboard.html',
  styleUrl: './seller-dashboard.scss'
})
export class SellerDashboardComponent implements OnInit {
  products: Product[] = [];
  loading = true;
  error = '';

  constructor(
    private productService: ProductService,
    public mediaService: MediaService
  ) {}

  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: all => {
        // filter client-side — backend will add /my-products once profile endpoint is ready
        this.products = all;
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load products'; this.loading = false; }
    });
  }

  delete(id: string): void {
    if (!confirm('Delete this product?')) return;
    this.productService.delete(id).subscribe({
      next: () => this.products = this.products.filter(p => p.id !== id),
      error: () => alert('Failed to delete')
    });
  }
}
