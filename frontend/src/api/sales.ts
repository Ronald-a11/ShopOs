import { apiRequest } from './client'

export type SaleLineInput = {
  productId: string
  quantity: number
  unitPrice: number
}

export type SalePaymentInput = {
  method: string
  amount: number
  providerReference?: string
}

export type SaleCommitResponse = {
  saleId: string
  receiptNumber: string | number
  inventory: { status: string }
  fiscal: { status: string; documentId?: string }
}

export type Sale = {
  id: string
  branchId: string
  deviceId: string
  shiftId: string
  clientSaleId: string
  receiptNumber: string | number
  currency: string
  total: number | string
  status: string
  occurredAt: string
}

export function commitSale(input: {
  clientSaleId: string
  branchId: string
  deviceId: string
  shiftId: string
  currency: string
  lines: SaleLineInput[]
  payments: SalePaymentInput[]
}): Promise<SaleCommitResponse> {
  return apiRequest<SaleCommitResponse>('/api/v1/sales', { method: 'POST', body: input })
}

export function listSales(branchId: string): Promise<Sale[]> {
  return apiRequest<Sale[]>(`/api/v1/sales?branchId=${encodeURIComponent(branchId)}`)
}

export function getSale(saleId: string): Promise<Sale> {
  return apiRequest<Sale>(`/api/v1/sales/${saleId}`)
}

export function requestReturn(saleId: string, body: unknown = {}): Promise<unknown> {
  return apiRequest(`/api/v1/sales/${saleId}/returns`, { method: 'POST', body })
}

export function requestVoid(saleId: string, body: unknown = {}): Promise<unknown> {
  return apiRequest(`/api/v1/sales/${saleId}/void-request`, { method: 'POST', body })
}
