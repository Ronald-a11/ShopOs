import { apiRequest } from './client'

export type Branch = {
  id: string
  code: string
  name: string
  address: string | null
  status: string
  createdAt: string
}

export type Device = {
  id: string
  branchId: string
  code: string
  name: string
  platform: string | null
  status: 'AVAILABLE' | 'IN_USE' | 'OFFLINE' | string
  createdAt: string
}

export function listBranches(): Promise<Branch[]> {
  return apiRequest<Branch[]>('/api/v1/branches')
}

export function getBranch(branchId: string): Promise<Branch> {
  return apiRequest<Branch>(`/api/v1/branches/${branchId}`)
}

export function listDevices(branchId: string): Promise<Device[]> {
  return apiRequest<Device[]>(`/api/v1/branches/${branchId}/devices`)
}

export function createBranch(input: { code: string; name: string; address?: string }): Promise<Branch> {
  return apiRequest<Branch>('/api/v1/branches', { method: 'POST', body: input })
}

export function registerDevice(
  branchId: string,
  input: { code: string; name: string; platform?: string },
): Promise<Device> {
  return apiRequest<Device>(`/api/v1/branches/${branchId}/devices`, { method: 'POST', body: input })
}
