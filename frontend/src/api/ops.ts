import { apiRequest } from './client'

export function syncBootstrap(): Promise<unknown> {
  return apiRequest('/api/v1/sync/bootstrap')
}

export function fiscalHealth(): Promise<unknown> {
  return apiRequest('/api/v1/fiscal/health')
}

export function listFiscalDevices(): Promise<unknown> {
  return apiRequest('/api/v1/fiscal/devices')
}

export function salesReport(params: {
  branchId?: string
  from?: string
  to?: string
} = {}): Promise<unknown> {
  const query = new URLSearchParams()
  if (params.branchId) query.set('branchId', params.branchId)
  if (params.from) query.set('from', params.from)
  if (params.to) query.set('to', params.to)
  const suffix = query.toString() ? `?${query}` : ''
  return apiRequest(`/api/v1/reports/sales${suffix}`)
}

export function cashReconciliation(shiftId: string): Promise<unknown> {
  return apiRequest(`/api/v1/reports/cash-reconciliation?shiftId=${encodeURIComponent(shiftId)}`)
}
