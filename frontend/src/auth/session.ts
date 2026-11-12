import type { AuthUser, TenantRole } from '../api/auth'
import type { Branch, Device } from '../api/branches'
import type { Shift } from '../api/shifts'

const USER_KEY = 'shopos.session.user'
const ROLE_KEY = 'shopos.session.role'
const BRANCH_KEY = 'shopos.session.branch'
const DEVICE_KEY = 'shopos.session.device'
const SHIFT_KEY = 'shopos.session.shift'

export type DemoAccount = {
  role: TenantRole
  label: string
  email: string
  password: string
  summary: string
}

/** Demo accounts aligned with technical-doc tenant roles. Shared password for local team use. */
export const DEMO_ACCOUNTS: DemoAccount[] = [
  {
    role: 'OWNER',
    label: 'Owner',
    email: 'owner@demo.shopos.co.zw',
    password: 'Password1!',
    summary: 'Subscription, fiscal devices, full control',
  },
  {
    role: 'ADMIN',
    label: 'Admin',
    email: 'admin@demo.shopos.co.zw',
    password: 'Password1!',
    summary: 'Users, branches, devices, catalogue, settings',
  },
  {
    role: 'MANAGER',
    label: 'Manager',
    email: 'manager@demo.shopos.co.zw',
    password: 'Password1!',
    summary: 'Reports, stock adjustments, sync review',
  },
  {
    role: 'SUPERVISOR',
    label: 'Supervisor',
    email: 'supervisor@demo.shopos.co.zw',
    password: 'Password1!',
    summary: 'Approve voids, returns, discounts, reconciliations',
  },
  {
    role: 'CASHIER',
    label: 'Cashier',
    email: 'cashier@demo.shopos.co.zw',
    password: 'Password1!',
    summary: 'Open shift, sell, record payments',
  },
  {
    role: 'STOCK_CONTROLLER',
    label: 'Stock controller',
    email: 'stock@demo.shopos.co.zw',
    password: 'Password1!',
    summary: 'Goods received, wastage, counts',
  },
]

export function saveSession(user: AuthUser, role: TenantRole) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
  sessionStorage.setItem(ROLE_KEY, role)
}

export function clearSession() {
  sessionStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(ROLE_KEY)
  sessionStorage.removeItem(BRANCH_KEY)
  sessionStorage.removeItem(DEVICE_KEY)
  sessionStorage.removeItem(SHIFT_KEY)
}

function readJson<T>(key: string): T | null {
  const raw = sessionStorage.getItem(key)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function saveSelectedBranch(branch: Branch) {
  sessionStorage.setItem(BRANCH_KEY, JSON.stringify(branch))
}

export function readSelectedBranch(): Branch | null {
  return readJson<Branch>(BRANCH_KEY)
}

export function saveSelectedDevice(device: Device) {
  sessionStorage.setItem(DEVICE_KEY, JSON.stringify(device))
}

export function readSelectedDevice(): Device | null {
  return readJson<Device>(DEVICE_KEY)
}

export function saveOpenShift(shift: Shift) {
  sessionStorage.setItem(SHIFT_KEY, JSON.stringify(shift))
}

export function readOpenShift(): Shift | null {
  return readJson<Shift>(SHIFT_KEY)
}

export function readSessionUser(): AuthUser | null {
  const raw = sessionStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

export function readSessionRole(): TenantRole | null {
  return sessionStorage.getItem(ROLE_KEY) as TenantRole | null
}

export function primaryRole(user: AuthUser): TenantRole {
  return user.memberships[0]?.role ?? 'CASHIER'
}

/** First route after a successful login for this role. */
export function entryRouteForRole(role: TenantRole): string {
  switch (role) {
    case 'CASHIER':
    case 'SUPERVISOR':
      return 'branch'
    default:
      return 'branch'
  }
}

/** Route after branch selection. */
export function afterBranchRoute(role: TenantRole | null): string {
  switch (role) {
    case 'CASHIER':
      return 'till'
    case 'SUPERVISOR':
      return 'till'
    case 'STOCK_CONTROLLER':
      return 'dashboard-stock'
    case 'MANAGER':
      return 'dashboard-manager'
    case 'ADMIN':
      return 'dashboard-admin'
    case 'OWNER':
      return 'dashboard-owner'
    default:
      return 'dashboard-cashier'
  }
}

/** Route after till selection. */
export function afterTillRoute(role: TenantRole | null): string {
  if (role === 'SUPERVISOR') return 'dashboard-supervisor'
  return 'shift'
}

export function dashboardRouteForRole(role: TenantRole | null): string {
  switch (role) {
    case 'OWNER':
      return 'dashboard-owner'
    case 'ADMIN':
      return 'dashboard-admin'
    case 'MANAGER':
      return 'dashboard-manager'
    case 'SUPERVISOR':
      return 'dashboard-supervisor'
    case 'STOCK_CONTROLLER':
      return 'dashboard-stock'
    case 'CASHIER':
    default:
      return 'dashboard-cashier'
  }
}
