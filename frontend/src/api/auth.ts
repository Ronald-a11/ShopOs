import { apiRequest, ApiError } from './client'

export { ApiError as AuthApiError }

export type TenantRole =
  | 'OWNER'
  | 'ADMIN'
  | 'MANAGER'
  | 'SUPERVISOR'
  | 'CASHIER'
  | 'STOCK_CONTROLLER'

export type Membership = {
  membershipId: string
  tenantId: string
  tenantName: string
  role: TenantRole
}

export type AuthUser = {
  id: string
  email: string
  displayName: string
  phone: string | null
  status: string
  emailVerified: boolean
  phoneVerified: boolean
  createdAt: string
  memberships: Membership[]
}

export type AuthResponse = {
  user: AuthUser
}

export type VerificationMethod = 'EMAIL' | 'PHONE'

export async function login(email: string, password: string, rememberMe = false): Promise<AuthUser> {
  const body = await apiRequest<AuthResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: { email, password, rememberMe },
  })
  return body.user
}

export async function logout(): Promise<void> {
  await apiRequest('/api/v1/auth/logout', { method: 'POST' })
}

export async function refreshSession(): Promise<void> {
  await apiRequest('/api/v1/auth/refresh', { method: 'POST' })
}

export async function me(): Promise<AuthUser> {
  return apiRequest<AuthUser>('/api/v1/me')
}

export async function forgotPassword(email: string): Promise<void> {
  await apiRequest('/api/v1/auth/forgot-password', {
    method: 'POST',
    body: { email },
  })
}

export async function resetPassword(token: string, password: string): Promise<void> {
  await apiRequest('/api/v1/auth/reset-password', {
    method: 'POST',
    body: { token, password },
  })
}

export async function sendVerification(method: VerificationMethod, value: string): Promise<void> {
  await apiRequest('/api/v1/auth/verification/send', {
    method: 'POST',
    body: { method, value },
  })
}

export async function confirmVerification(
  method: VerificationMethod,
  value: string,
  code: string,
): Promise<void> {
  await apiRequest('/api/v1/auth/verification/confirm', {
    method: 'POST',
    body: { method, value, code },
  })
}
