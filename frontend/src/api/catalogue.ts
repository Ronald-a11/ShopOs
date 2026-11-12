import { apiRequest } from './client'

export type Product = {
  id: string
  sku: string
  name: string
  barcode: string | null
  unitPrice: number | string
  taxRateCode: string | null
  status: string
}

export function listProducts(params: { q?: string; barcode?: string } = {}): Promise<Product[]> {
  const query = new URLSearchParams()
  if (params.q) query.set('q', params.q)
  if (params.barcode) query.set('barcode', params.barcode)
  const suffix = query.toString() ? `?${query}` : ''
  return apiRequest<Product[]>(`/api/v1/products${suffix}`)
}

export function getProduct(productId: string): Promise<Product> {
  return apiRequest<Product>(`/api/v1/products/${productId}`)
}

export function createProduct(input: {
  sku: string
  name: string
  barcode?: string
  unitPrice: number
  taxRateCode?: string
}): Promise<Product> {
  return apiRequest<Product>('/api/v1/products', { method: 'POST', body: input })
}
