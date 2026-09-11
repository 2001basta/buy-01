import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { sellerGuard } from './guards/seller.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },

  // Public auth routes
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./components/login/login').then(m => m.LoginComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'auth/signup',
    loadComponent: () =>
      import('./components/signup/signup').then(m => m.SignupComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'profile',
    loadComponent: () =>
      import('./components/profile/profile').then(m => m.ProfileComponent),
    canActivate: [authGuard]
  },

  // Public product browsing
  {
    path: 'products',
    loadComponent: () =>
      import('./components/product-list/product-list').then(m => m.ProductListComponent)
  },
  {
    path: 'products/:id',
    loadComponent: () =>
      import('./components/product-detail/product-detail').then(m => m.ProductDetailComponent)
  },

  // Seller-only routes
  {
    path: 'seller/dashboard',
    loadComponent: () =>
      import('./components/seller-dashboard/seller-dashboard').then(m => m.SellerDashboardComponent),
    canActivate: [authGuard, sellerGuard]
  },
  {
    path: 'seller/products/create',
    loadComponent: () =>
      import('./components/create-product/create-product').then(m => m.CreateProductComponent),
    canActivate: [authGuard, sellerGuard]
  },
  {
    path: 'seller/products/edit/:id',
    loadComponent: () =>
      import('./components/create-product/create-product').then(m => m.CreateProductComponent),
    canActivate: [authGuard, sellerGuard]
  },

  { path: '**', redirectTo: '/products' }
];
