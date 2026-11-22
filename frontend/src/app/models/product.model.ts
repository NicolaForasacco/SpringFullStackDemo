export interface Product {
  id?: number;
  name: string;
  price: number;
  description?: string;
}

export interface ProductListResponse {
  products: Product[];
  source: 'REDIS_CACHE' | 'POSTGRESQL';
  elapsedMs: number;
}
