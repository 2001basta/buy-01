import { Component, OnInit } from '@angular/core';
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
  products: Product[] = [];
  loading = true;
  error = '';

  constructor(
    private productService: ProductService,
    public mediaService: MediaService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: products => { this.products = products; this.loading = false; },
      error: () => { this.error = 'Failed to load products'; this.loading = false; }
    });
  }
}
