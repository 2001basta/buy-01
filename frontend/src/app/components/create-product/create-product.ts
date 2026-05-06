import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../services/product.service';
import { MediaService } from '../../services/media.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-create-product',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './create-product.html',
  styleUrl: './create-product.scss'
})
export class CreateProductComponent implements OnInit {
  form: FormGroup;
  product: Product | null = null;
  pendingFiles: File[] = [];
  uploading = false;
  saving = false;
  error = '';
  isEdit = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private mediaService: MediaService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      price: [null, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.productService.getById(id).subscribe({
        next: p => { this.product = p; this.form.patchValue(p); },
        error: () => this.error = 'Failed to load product'
      });
    }
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) this.pendingFiles = Array.from(input.files);
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving = true;
    this.error = '';

    if (this.isEdit && this.product) {
      this.productService.update(this.product.id, this.form.value).subscribe({
        next: p => this.uploadImages(p.id),
        error: err => { this.error = err.error?.message ?? 'Update failed'; this.saving = false; }
      });
    } else {
      this.productService.create(this.form.value).subscribe({
        next: p => this.uploadImages(p.id),
        error: err => { this.error = err.error?.message ?? 'Create failed'; this.saving = false; }
      });
    }
  }

  private uploadImages(productId: string): void {
    if (!this.pendingFiles.length) {
      this.router.navigate(['/seller/dashboard']);
      return;
    }
    this.uploading = true;
    const uploads = this.pendingFiles.map(f =>
      this.mediaService.upload(f, productId).toPromise()
    );

    Promise.all(uploads).then(results => {
      const imageIds = results.map(r => r!.id);
      this.productService.attachImages(productId, imageIds).subscribe({
        next: () => this.router.navigate(['/seller/dashboard']),
        error: () => { this.error = 'Product saved but image attach failed'; this.saving = false; }
      });
    }).catch(() => {
      this.error = 'Image upload failed (check size ≤ 2MB, jpeg/png only)';
      this.saving = false;
      this.uploading = false;
    });
  }
}
