import { Component, OnInit, WritableSignal, signal } from '@angular/core';
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
  products: WritableSignal<Product[]> = signal([]);
  loading = signal(true);
  error = signal('');

  constructor(
    private productService: ProductService,
    public mediaService: MediaService
  ) {}

  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: all => { this.products.set(all); this.loading.set(false); },
      error: () => { this.error.set('Failed to load products'); this.loading.set(false); }
    });
  }

  delete(id: string): void {
    if (!confirm('Delete this product?')) return;
    this.productService.delete(id).subscribe({
      next: () => this.products.update(list => list.filter(p => p.id !== id)),
      error: () => alert('Failed to delete')
    });
  }
}
