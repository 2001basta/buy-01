import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { sellerGuard } from './guards/seller.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },

  // Public auth routes
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./components/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'auth/signup',
    loadComponent: () =>
      import('./components/signup/signup').then(m => m.SignupComponent)
  },

  // Public product browsing
  {
    path: 'products',
    loadComponent: () =>
      import('./components/product-list/product-list').then(m => m.ProductListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'products/:id',
    loadComponent: () =>
      import('./components/product-detail/product-detail').then(m => m.ProductDetailComponent),
    canActivate: [authGuard]
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
