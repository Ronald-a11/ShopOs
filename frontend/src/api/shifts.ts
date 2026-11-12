import { apiRequest } from './client'

export type Shift = {
  id: string
  branchId: string
  deviceId: string
  userId: string
  status: string
  openingFloat: number | string
  countedCash: number | string | null
  expectedCash: number | string | null
  variance: number | string | null
  note: string | null
  openedAt: string
  closedAt: string | null
}

export function openShift(input: {
  branchId: string
  deviceId: string
  openingFloat: number
}): Promise<Shift> {
  return apiRequest<Shift>('/api/v1/shifts/open', { method: 'POST', body: input })
}

export function currentShift(deviceId: string): Promise<Shift> {
  return apiRequest<Shift>(`/api/v1/shifts/current?deviceId=${encodeURIComponent(deviceId)}`)
}

export function addCashMovement(
  shiftId: string,
  input: { type: 'PAID_IN' | 'PAID_OUT' | 'DROP'; amount: number; note?: string },
): Promise<unknown> {
  return apiRequest(`/api/v1/shifts/${shiftId}/cash-movements`, { method: 'POST', body: input })
}

export function closeShift(
  shiftId: string,
  input: { countedCash: number; note?: string },
): Promise<Shift> {
  return apiRequest<Shift>(`/api/v1/shifts/${shiftId}/close`, { method: 'POST', body: input })
}
