export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  sellerId: string;
  imageIds: string[];
  createdAt: string;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  price?: number;
}
