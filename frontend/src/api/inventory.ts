import { apiRequest } from './client'

export type InventoryBalance = {
  branchId: string
  productId: string
  quantity: number | string
  updatedAt: string
}

export type InventoryMovement = {
  id: string
  branchId: string
  productId: string
  quantityDelta: number | string
  movementType: string
  referenceType: string | null
  referenceId: string | null
  note: string | null
  createdAt: string
}

export function listBranchInventory(branchId: string): Promise<InventoryBalance[]> {
  return apiRequest<InventoryBalance[]>(`/api/v1/branches/${branchId}/inventory`)
}

export function receiveStock(input: {
  branchId: string
  lines: { productId: string; quantity: number }[]
  note?: string
}): Promise<unknown> {
  return apiRequest('/api/v1/inventory/receipts', { method: 'POST', body: input })
}

export function adjustStock(input: {
  branchId: string
  productId: string
  quantityDelta: number
  reason: string
}): Promise<unknown> {
  return apiRequest('/api/v1/inventory/adjustments', { method: 'POST', body: input })
}

export function listMovements(branchId: string): Promise<InventoryMovement[]> {
  return apiRequest<InventoryMovement[]>(
    `/api/v1/inventory/movements?branchId=${encodeURIComponent(branchId)}`,
  )
}

export function listLowStock(branchId: string, threshold = 10): Promise<InventoryBalance[]> {
  return apiRequest<InventoryBalance[]>(
    `/api/v1/inventory/low-stock?branchId=${encodeURIComponent(branchId)}&threshold=${threshold}`,
  )
}
