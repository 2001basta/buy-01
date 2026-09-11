import { Component, OnInit, WritableSignal, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../services/product.service';
import { MediaService } from '../../services/media.service';
import { Product } from '../../models/product.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-create-product',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './create-product.html',
  styleUrl: './create-product.scss'
})
export class CreateProductComponent implements OnInit {
  private readonly maxFileSize = 2 * 1024 * 1024;
  private readonly allowedTypes = new Set(['image/jpeg', 'image/png']);
  form: FormGroup;
  product: WritableSignal<Product | null> = signal(null);
  pendingFiles: WritableSignal<File[]> = signal([]);
  uploading = signal(false);
  saving = signal(false);
  error = signal('');
  fileError = signal('');
  previews = signal<string[]>([]);
  existingImageIds = signal<string[]>([]);
  isEdit = signal(false);

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    public mediaService: MediaService,
    public auth: AuthService
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
      this.isEdit.set(true);
      this.productService.getById(id).subscribe({
        next: p => { this.product.set(p); this.existingImageIds.set(p.imageIds); this.form.patchValue(p); },
        error: () => this.error.set('Failed to load product')
      });
    }
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fileError.set('');
    this.previews().forEach(url => URL.revokeObjectURL(url));
    const files = Array.from(input.files ?? []);
    const invalid = files.find(file => !this.allowedTypes.has(file.type) || file.size > this.maxFileSize);
    if (invalid) {
      this.pendingFiles.set([]);
      this.previews.set([]);
      this.fileError.set(`${invalid.name} must be a JPEG or PNG image no larger than 2 MB.`);
      input.value = '';
      return;
    }
    this.pendingFiles.set(files);
    this.previews.set(files.map(file => URL.createObjectURL(file)));
  }

  removeFile(index: number): void {
    const urls = this.previews();
    if (urls[index]) URL.revokeObjectURL(urls[index]);
    this.pendingFiles.update(files => files.filter((_, fileIndex) => fileIndex !== index));
    this.previews.update(items => items.filter((_, previewIndex) => previewIndex !== index));
  }

  removeExistingImage(imageId: string): void {
    if (!this.product()) return;
    this.productService.removeImage(this.product()!.id, imageId).subscribe({
      next: product => {
        this.existingImageIds.set(product.imageIds);
        this.mediaService.delete(imageId).subscribe({
          error: err => this.error.set(err.error?.message ?? 'Image reference removed, but file cleanup failed')
        });
      },
      error: err => this.error.set(err.error?.message ?? 'Unable to remove image from product')
    });
  }

  submit(): void {
    if (this.form.invalid || this.fileError()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');

    if (this.isEdit() && this.product()) {
      this.productService.update(this.product()!.id, this.form.value).subscribe({
        next: p => this.uploadImages(p.id),
        error: err => { this.error.set(err.error?.message ?? 'Update failed'); this.saving.set(false); }
      });
    } else {
      this.productService.create(this.form.value).subscribe({
        next: p => this.uploadImages(p.id),
        error: err => { this.error.set(err.error?.message ?? 'Create failed'); this.saving.set(false); }
      });
    }
  }

  private uploadImages(productId: string): void {
    if (!this.pendingFiles().length) {
      this.router.navigate(['/seller/dashboard']);
      return;
    }
    this.uploading.set(true);
    const uploads = this.pendingFiles().map(file =>
      this.mediaService.upload(file, productId).toPromise()
    );

    Promise.allSettled(uploads).then(results => {
      const imageIds = results
        .filter(result => result.status === 'fulfilled')
        .map(result => result.value?.id)
        .filter((id): id is string => !!id);
      const failed = results.some(result => result.status === 'rejected');
      if (failed) {
        this.cleanupMedia(imageIds);
        this.error.set('Some images could not be uploaded. Please check each file is a JPEG or PNG under 2 MB.');
        this.saving.set(false);
        this.uploading.set(false);
        return;
      }
      const allImageIds = [...this.existingImageIds(), ...imageIds];
      this.productService.attachImages(productId, allImageIds).subscribe({
        next: () => this.router.navigate(['/seller/dashboard']),
        error: err => {
          this.cleanupMedia(imageIds);
          const message = err.error?.message ?? 'Image attachment failed';
          this.error.set(`Product saved, but images could not be attached: ${message}`);
          this.saving.set(false);
          this.uploading.set(false);
        }
      });
    });
  }

  private cleanupMedia(imageIds: string[]): void {
    imageIds.forEach(id => this.mediaService.delete(id).subscribe({ error: () => undefined }));
  }
}
