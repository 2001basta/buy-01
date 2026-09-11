import { Component, OnInit, WritableSignal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { MediaService } from '../../services/media.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-detail',
  imports: [CommonModule, RouterLink],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss'
})
export class ProductDetailComponent implements OnInit {
  product: WritableSignal<Product | null> = signal(null);
  loading = signal(true);
  error = signal('');
  deleteDialogOpen = signal(false);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    public mediaService: MediaService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.productService.getById(id).subscribe({
      next: p => { this.product.set(p); this.loading.set(false); },
      error: () => { this.error.set('Product not found'); this.loading.set(false); }
    });
  }

  delete(): void {
    if (!this.product()) return;
    this.deleteDialogOpen.set(true);
  }

  cancelDelete(): void {
    this.deleteDialogOpen.set(false);
  }

  confirmDelete(): void {
    if (!this.product()) return;
    this.deleteDialogOpen.set(false);
    this.productService.delete(this.product()!.id).subscribe({
      next: () => this.router.navigate(['/products']),
      error: err => this.error.set(err.error?.message ?? 'You are not allowed to delete this product')
    });
  }

  canManageProduct(): boolean {
    return this.auth.isSeller() && this.product()?.sellerId === this.auth.getUserId();
  }
}
