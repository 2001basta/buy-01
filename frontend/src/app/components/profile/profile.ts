import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProfileService } from '../../services/profile.service';
import { MediaService } from '../../services/media.service';
import { UserProfile } from '../../models/user.model';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class ProfileComponent implements OnInit {
  profile = signal<UserProfile | null>(null);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  avatarError = signal('');

  form: FormGroup<{
    name: FormControl<string>;
    email: FormControl<string>;
  }>;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    public mediaService: MediaService
  ) {
    this.form = this.fb.nonNullable.group({
      name: ['', [Validators.required, Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.profileService.get().subscribe({
      next: profile => {
        this.profile.set(profile);
        this.form.patchValue({ name: profile.name, email: profile.email });
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load your profile.');
        this.loading.set(false);
      }
    });
  }

  onAvatarChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.avatarError.set('');
    if (!file) return;
    if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 2 * 1024 * 1024) {
      this.avatarError.set('Avatar must be a JPEG or PNG image no larger than 2 MB.');
      input.value = '';
      return;
    }

    this.saving.set(true);
    this.mediaService.upload(file).subscribe({
      next: media => this.saveProfile(media.id),
      error: err => {
        this.avatarError.set(err.error?.message ?? 'Avatar upload failed.');
        this.saving.set(false);
      }
    });
  }

  save(): void {
    if (this.form.invalid || !this.profile()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saveProfile(this.profile()!.avatar);
  }

  private saveProfile(avatar?: string): void {
    this.saving.set(true);
    this.error.set('');
    this.profileService.update({
      name: this.form.controls.name.value.trim(),
      email: this.form.controls.email.value.trim(),
      ...(avatar ? { avatar } : {})
    }).subscribe({
      next: profile => {
        this.profile.set(profile);
        this.form.patchValue({ name: profile.name, email: profile.email });
        this.saving.set(false);
      },
      error: err => {
        this.error.set(err.error?.message ?? 'Profile update failed.');
        this.saving.set(false);
      }
    });
  }
}
