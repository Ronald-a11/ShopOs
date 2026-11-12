import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { ApiError } from '../api/client'
import {
  confirmVerification,
  forgotPassword,
  logout as apiLogout,
  me,
  refreshSession,
  resetPassword,
  sendVerification,
  type VerificationMethod,
} from '../api/auth'
import { listBranches, listDevices, type Branch, type Device } from '../api/branches'
import { openShift } from '../api/shifts'
import { CompactBrand, LoginStoryPanel } from './LoginScreen'
import {
  afterBranchRoute,
  afterTillRoute,
  clearSession,
  dashboardRouteForRole,
  primaryRole,
  readSelectedBranch,
  readSelectedDevice,
  readSessionRole,
  readSessionUser,
  saveOpenShift,
  saveSelectedBranch,
  saveSelectedDevice,
  saveSession,
} from './session'
import './Dashboard.css'
import './LoginScreen.css'

function hashParam(name: string): string {
  const hash = window.location.hash
  const queryIndex = hash.indexOf('?')
  if (queryIndex < 0) return ''
  return new URLSearchParams(hash.slice(queryIndex + 1)).get(name) ?? ''
}

type IconName =
  | 'clock'
  | 'logout'
  | 'shield'
  | 'block'
  | 'monitor'
  | 'restore'
  | 'lock'
  | 'mail'
  | 'info'
  | 'cash'
  | 'cart'
  | 'chart'
  | 'box'
  | 'users'
  | 'alert'
  | 'receipt'
  | 'truck'

function AuthIcon({ name }: { name: IconName }) {
  const paths: Record<IconName, ReactNode> = {
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3.5 2" /></>,
    logout: <><path d="M10 5H5v14h5M13 8l4 4-4 4M17 12H9" /></>,
    shield: <><path d="M12 3 20 6v5c0 5-3.2 8.3-8 10-4.8-1.7-8-5-8-10V6l8-3Z" /><path d="m9 12 2 2 4-4" /></>,
    block: <><circle cx="12" cy="12" r="8" /><path d="m7.5 16.5 9-9" /></>,
    monitor: <><rect x="3" y="4" width="18" height="13" rx="2" /><path d="M8 21h8M12 17v4" /></>,
    restore: <><path d="M20 11a8 8 0 1 0-2.3 5.7M20 4v7h-7" /></>,
    lock: <><rect x="5" y="10" width="14" height="11" rx="2" /><path d="M8 10V7a4 4 0 0 1 8 0v3" /></>,
    mail: <><rect x="3" y="5" width="18" height="14" rx="2" /><path d="m4 7 8 6 8-6" /></>,
    info: <><circle cx="12" cy="12" r="9" /><path d="M12 11v6M12 7.5v.5" /></>,
    cash: <><rect x="2" y="6" width="20" height="12" rx="2" /><circle cx="12" cy="12" r="2.5" /><path d="M6 12h.01M18 12h.01" /></>,
    cart: <><path d="M2 4h3l2 11h10l2-7H6M9 19h8M8 19a1 1 0 1 0 0 2 1 1 0 0 0 0-2ZM17 19a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z" /></>,
    chart: <><path d="M4 19V5M4 19h17M8 15l4-4 3 2 5-6" /><path d="M17 7h3v3" /></>,
    box: <><path d="m3 7 9-4 9 4v10l-9 4-9-4V7Z" /><path d="m3 7 9 4 9-4M12 11v10M7.5 5l9 4" /></>,
    users: <><circle cx="9" cy="8" r="3" /><path d="M3 20v-1a6 6 0 0 1 12 0v1M16 5.2a3 3 0 0 1 0 5.6M18 14a5 5 0 0 1 3 4.6v1" /></>,
    alert: <><path d="M12 3 2.8 19h18.4L12 3Z" /><path d="M12 9v4M12 16.5v.01" /></>,
    receipt: <><path d="M5 3h14v18l-3-2-4 2-4-2-3 2V3Z" /><path d="M8 8h8M8 12h8M8 16h3" /></>,
    truck: <><path d="M3 6h11v11H3zM14 10h4l3 3v4h-7" /><circle cx="7.5" cy="18" r="1.5" /><circle cx="17.5" cy="18" r="1.5" /></>,
  }

  return <svg className="auth-icon" viewBox="0 0 24 24" aria-hidden="true">{paths[name]}</svg>
}

function go(route: string) {
  window.location.hash = route
}

function AuthLayout({ children, wide = false }: { children: ReactNode; wide?: boolean }) {
  const currentRoute = window.location.hash.slice(1)

  return (
    <main key={currentRoute} className="login-screen">
      <LoginStoryPanel animateLogo />
      <section className={`auth-panel-wrap${wide ? ' auth-panel-wrap--wide' : ''}`} aria-label="Authentication">
        <CompactBrand />
        <div className="auth-panel">
          {children}
        </div>
      </section>
    </main>
  )
}

function BackButton({ route = 'login', label = 'Back to sign in' }: { route?: string; label?: string }) {
  return <button className="auth-back" type="button" onClick={() => go(route)}>← {label}</button>
}

function SecureFooter() {
  return (
    <div className="auth-footer">
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <rect x="6" y="10" width="12" height="10" rx="2" />
        <path d="M9 10V7a3 3 0 0 1 6 0v3M12 14v2" />
      </svg>
      <span>SECURE &amp; ENCRYPTED</span>
    </div>
  )
}

function ActionButton({ children, type = 'button', onClick, secondary = false, disabled = false }: {
  children: ReactNode
  type?: 'button' | 'submit'
  onClick?: () => void
  secondary?: boolean
  disabled?: boolean
}) {
  return (
    <button
      className={`auth-action${secondary ? ' auth-action--secondary' : ''}`}
      type={type}
      onClick={onClick}
      disabled={disabled}
    >
      <span>{children}</span>
      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h13M13 7l5 5-5 5" /></svg>
    </button>
  )
}

export function ForgotPasswordScreen() {
  const [email, setEmail] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [sent, setSent] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await forgotPassword(email)
      setSent(true)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not send recovery link')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthLayout>
      <h1 className="auth-title">Forgot password?</h1>
      <p className="auth-subtitle">Enter your email address and we'll send you a link to reset your password.</p>
      {sent ? (
        <>
          <div className="auth-notice auth-notice--info">
            <AuthIcon name="mail" />
            <div>
              <strong>Check your email</strong>
              <span>If an account exists for {email}, a recovery link was sent. Locally the token is also printed in the API logs.</span>
            </div>
          </div>
          <ActionButton onClick={() => go('reset')}>Enter reset token</ActionButton>
        </>
      ) : (
        <form onSubmit={(event) => void handleSubmit(event)}>
          <label className="auth-label" htmlFor="recovery-email">Email address</label>
          <div className="auth-input">
            <AuthIcon name="mail" />
            <input
              id="recovery-email"
              type="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="name@yourstore.co.zw"
            />
          </div>
          {error ? <p className="login-error" role="alert">{error}</p> : null}
          <ActionButton type="submit" disabled={busy}>{busy ? 'Sending…' : 'Send recovery link'}</ActionButton>
        </form>
      )}
      <div className="auth-divider"><span>OR</span></div>
      <button className="auth-secondary-wide" type="button" onClick={() => go('login')}>Back to sign in</button>
      <SecureFooter />
    </AuthLayout>
  )
}

export function ResetPasswordScreen() {
  const [showNew, setShowNew] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [token, setToken] = useState(() => hashParam('token'))
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (password !== confirm) {
      setError('Passwords do not match')
      return
    }
    setBusy(true)
    setError(null)
    try {
      await resetPassword(token.trim(), password)
      go('login')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not reset password')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthLayout>
      <BackButton route="login" />
      <h1 className="auth-title">Reset Password</h1>
      <p className="auth-subtitle">Create a new password for your ShopOS account.</p>
      <form onSubmit={(event) => void handleSubmit(event)}>
        <label className="auth-label" htmlFor="reset-token">Reset token</label>
        <div className="auth-input">
          <AuthIcon name="shield" />
          <input
            id="reset-token"
            type="text"
            required
            value={token}
            onChange={(event) => setToken(event.target.value)}
            placeholder="Paste the token from your email or API logs"
          />
        </div>
        <label className="auth-label" htmlFor="new-password">New password</label>
        <div className="auth-input">
          <AuthIcon name="lock" />
          <input
            id="new-password"
            type={showNew ? 'text' : 'password'}
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Enter new password"
          />
          <button type="button" className="auth-eye" aria-label="Show password" onClick={() => setShowNew(v => !v)}>◉</button>
        </div>
        <ul className="auth-requirements">
          <li>At least 8 characters</li>
          <li>One uppercase letter</li>
          <li>One number</li>
          <li>One special character (e.g. !@#$%)</li>
        </ul>
        <label className="auth-label" htmlFor="confirm-password">Confirm new password</label>
        <div className="auth-input">
          <AuthIcon name="lock" />
          <input
            id="confirm-password"
            type={showConfirm ? 'text' : 'password'}
            required
            value={confirm}
            onChange={(event) => setConfirm(event.target.value)}
            placeholder="Confirm new password"
          />
          <button type="button" className="auth-eye" aria-label="Show password" onClick={() => setShowConfirm(v => !v)}>◉</button>
        </div>
        {error ? <p className="login-error" role="alert">{error}</p> : null}
        <ActionButton type="submit" disabled={busy}>{busy ? 'Saving…' : 'Reset password'}</ActionButton>
      </form>
      <SecureFooter />
    </AuthLayout>
  )
}

export function AccountVerificationScreen() {
  const [method, setMethod] = useState<'email' | 'phone'>('email')
  const [value, setValue] = useState('')
  const [code, setCode] = useState('')
  const [awaitingCode, setAwaitingCode] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [info, setInfo] = useState<string | null>(null)

  const apiMethod: VerificationMethod = method === 'email' ? 'EMAIL' : 'PHONE'

  async function handleSend() {
    setBusy(true)
    setError(null)
    setInfo(null)
    try {
      await sendVerification(apiMethod, value)
      setAwaitingCode(true)
      setInfo('Verification code sent. Locally it is also printed in the API logs.')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not send verification code')
    } finally {
      setBusy(false)
    }
  }

  async function handleConfirm(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await confirmVerification(apiMethod, value, code.trim())
      go('login')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not confirm verification')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthLayout>
      <BackButton route="login" />
      <h1 className="auth-title">Account Verification</h1>
      <p className="auth-subtitle">Verify/activate your account, if required.</p>
      <div className="auth-notice auth-notice--info">
        <AuthIcon name="shield" />
        <div><strong>We're almost there!</strong><span>To keep your account secure, please verify your email address or phone number.</span></div>
      </div>
      <div className="auth-label">Verification method</div>
      <div className="auth-methods">
        <button type="button" className={method === 'email' ? 'active' : ''} onClick={() => setMethod('email')}>
          <span className="auth-radio" />Email verification
        </button>
        <button type="button" className={method === 'phone' ? 'active' : ''} onClick={() => setMethod('phone')}>
          <span className="auth-radio" />Phone verification
        </button>
      </div>
      <label className="auth-label" htmlFor="verification-value">{method === 'email' ? 'Email address' : 'Phone number'}</label>
      <div className="auth-input">
        {method === 'email' ? <AuthIcon name="mail" /> : <span className="auth-input-plus">+</span>}
        <input
          id="verification-value"
          type={method === 'email' ? 'email' : 'tel'}
          value={value}
          onChange={(event) => setValue(event.target.value)}
          placeholder={method === 'email' ? 'name@yourstore.co.zw' : '+263771000000'}
        />
      </div>
      {awaitingCode ? (
        <form onSubmit={(event) => void handleConfirm(event)}>
          <label className="auth-label" htmlFor="verification-code">Verification code</label>
          <div className="auth-input">
            <AuthIcon name="lock" />
            <input
              id="verification-code"
              type="text"
              required
              inputMode="numeric"
              value={code}
              onChange={(event) => setCode(event.target.value)}
              placeholder="6-digit code"
            />
          </div>
          {info ? <p className="auth-subtitle">{info}</p> : null}
          {error ? <p className="login-error" role="alert">{error}</p> : null}
          <ActionButton type="submit" disabled={busy}>{busy ? 'Confirming…' : 'Confirm code'}</ActionButton>
          <button className="auth-text-link" type="button" disabled={busy} onClick={() => void handleSend()}>
            Resend code
          </button>
        </form>
      ) : (
        <>
          {error ? <p className="login-error" role="alert">{error}</p> : null}
          <ActionButton disabled={busy || !value.trim()} onClick={() => void handleSend()}>
            {busy ? 'Sending…' : 'Send verification code'}
          </ActionButton>
          <div className="auth-divider"><span>OR</span></div>
          <button className="auth-text-link" type="button" onClick={() => setAwaitingCode(true)}>
            I already have a verification code
          </button>
        </>
      )}
      <SecureFooter />
    </AuthLayout>
  )
}

export function SessionExpiredScreen() {
  return (
    <AuthLayout>
      <div className="auth-status-icon"><AuthIcon name="clock" /></div>
      <h1 className="auth-title">Session expired</h1>
      <p className="auth-subtitle">Your session has timed out. Please authenticate again to continue.</p>
      <div className="auth-notice"><AuthIcon name="info" /><span>Your account is safe. No changes were made while you were signed out.</span></div>
      <ActionButton onClick={() => go('login')}>Sign in again</ActionButton>
      <ActionButton onClick={() => go('till')} secondary>Open till</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

export function LogoutScreen() {
  const role = readSessionRole()
  const stayRoute =
    role === 'OWNER' ? 'dashboard-owner'
      : role === 'ADMIN' ? 'dashboard-admin'
        : role === 'MANAGER' ? 'dashboard-manager'
          : role === 'SUPERVISOR' ? 'dashboard-supervisor'
            : role === 'STOCK_CONTROLLER' ? 'dashboard-stock'
              : 'dashboard-cashier'

  async function handleLogout() {
    try {
      await apiLogout()
    } catch {
      // Session may already be gone; still clear local state.
    }
    clearSession()
    go('login')
  }

  return (
    <AuthLayout>
      <BackButton route={stayRoute} label="Back to dashboard" />
      <div className="auth-status-icon"><AuthIcon name="logout" /></div>
      <h1 className="auth-title">Log out of ShopOS?</h1>
      <p className="auth-subtitle">You will need to sign in again to access ShopOS.</p>
      <div className="auth-notice"><AuthIcon name="info" /><span>Finish or hold any open sale before logging out so nothing is left pending.</span></div>
      <ActionButton onClick={() => void handleLogout()}>Log out</ActionButton>
      <ActionButton secondary onClick={() => go(stayRoute)}>Stay signed in</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

export function AccessDeniedScreen() {
  const role = readSessionRole()
  const back =
    role === 'OWNER' ? 'dashboard-owner'
      : role === 'ADMIN' ? 'dashboard-admin'
        : role === 'MANAGER' ? 'dashboard-manager'
          : role === 'SUPERVISOR' ? 'dashboard-supervisor'
            : role === 'STOCK_CONTROLLER' ? 'dashboard-stock'
              : 'dashboard-cashier'

  return (
    <AuthLayout>
      <BackButton route={back} label="Back to dashboard" />
      <div className="auth-status-icon"><AuthIcon name="shield" /></div>
      <h1 className="auth-title">Access denied</h1>
      <p className="auth-subtitle">This role would normally be blocked. In the mock-up you can continue anyway.</p>
      <div className="auth-notice"><AuthIcon name="info" /><span>Requested page: Reports. Required role: Manager.</span></div>
      <ActionButton onClick={() => go(back)}>Back to dashboard</ActionButton>
      <ActionButton secondary onClick={() => go(back)}>Request access</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

export function AccountDisabledScreen() {
  return (
    <AuthLayout>
      <BackButton route="login" />
      <div className="auth-status-icon"><AuthIcon name="block" /></div>
      <h1 className="auth-title">Account disabled</h1>
      <p className="auth-subtitle">Your account has been disabled and can't be used to sign in right now.</p>
      <div className="auth-notice"><AuthIcon name="info" /><span>Access to ShopOS is restricted for this account. Contact your administrator to have it re-enabled.</span></div>
      <ActionButton>Contact administrator</ActionButton>
      <ActionButton secondary onClick={() => go('login')}>Back to sign in</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

function deviceStatusLabel(status: string): string {
  switch (status) {
    case 'AVAILABLE':
      return 'Available'
    case 'IN_USE':
      return 'In use'
    case 'OFFLINE':
      return 'Offline'
    default:
      return status
  }
}

export function SelectBranchScreen() {
  const [branches, setBranches] = useState<Branch[]>([])
  const [selected, setSelected] = useState(0)
  const [busy, setBusy] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const rows = await listBranches()
        if (cancelled) return
        setBranches(rows)
        const saved = readSelectedBranch()
        const index = saved ? rows.findIndex((branch) => branch.id === saved.id) : 0
        setSelected(index >= 0 ? index : 0)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : 'Could not load branches')
        }
      } finally {
        if (!cancelled) setBusy(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  function handleContinue() {
    const branch = branches[selected]
    if (!branch) return
    saveSelectedBranch(branch)
    go(afterBranchRoute(readSessionRole()))
  }

  return (
    <AuthLayout wide>
      <h1 className="auth-title">Select a branch</h1>
      <p className="auth-subtitle">Choose the branch you're working at today.</p>
      {busy ? <p className="auth-subtitle">Loading branches…</p> : null}
      {error ? <p className="login-error" role="alert">{error}</p> : null}
      <div className="auth-options auth-options--branch">
        {branches.map((branch, index) => (
          <button
            key={branch.id}
            className={`auth-option${selected === index ? ' active' : ''}`}
            type="button"
            onClick={() => setSelected(index)}
          >
            <span><strong>{branch.name}</strong><small>{branch.code}</small></span>
            <em>{selected === index ? 'Selected' : branch.status === 'ACTIVE' ? 'Available' : branch.status}</em>
          </button>
        ))}
      </div>
      <ActionButton disabled={busy || !branches[selected]} onClick={handleContinue}>Continue</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

export function SelectTillScreen() {
  const branch = readSelectedBranch()
  const [devices, setDevices] = useState<Device[]>([])
  const [selected, setSelected] = useState(0)
  const [busy, setBusy] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!branch) {
      go('branch')
      return
    }
    let cancelled = false
    void (async () => {
      try {
        const rows = await listDevices(branch.id)
        if (cancelled) return
        setDevices(rows)
        const saved = readSelectedDevice()
        const index = saved ? rows.findIndex((device) => device.id === saved.id) : 0
        setSelected(index >= 0 ? index : 0)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : 'Could not load tills')
        }
      } finally {
        if (!cancelled) setBusy(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [branch?.id])

  const selectedDevice = devices[selected]
  const canOpen = selectedDevice?.status === 'AVAILABLE'

  function handleContinue() {
    if (!selectedDevice || !canOpen) return
    saveSelectedDevice(selectedDevice)
    go(afterTillRoute(readSessionRole()))
  }

  return (
    <AuthLayout wide>
      <BackButton route="branch" label="Back to branches" />
      <div className="auth-status-icon"><AuthIcon name="monitor" /></div>
      <h1 className="auth-title">Select a till</h1>
      <p className="auth-subtitle">
        Choose the device for General retail at {branch?.name ?? 'your branch'}.
      </p>
      {busy ? <p className="auth-subtitle">Loading tills…</p> : null}
      {error ? <p className="login-error" role="alert">{error}</p> : null}
      <div className="auth-options">
        {devices.map((device, index) => {
          const unavailable = device.status !== 'AVAILABLE'
          return (
            <button
              key={device.id}
              className={`auth-option${selected === index ? ' active' : ''}${unavailable ? ' auth-option--disabled' : ''}`}
              type="button"
              disabled={unavailable}
              onClick={() => setSelected(index)}
            >
              <span><strong>{device.name}</strong><small>{device.code}</small></span>
              <em>{deviceStatusLabel(device.status)}</em>
            </button>
          )
        })}
      </div>
      <ActionButton disabled={busy || !canOpen} onClick={handleContinue}>
        {readSessionRole() === 'SUPERVISOR' ? 'Continue to floor' : 'Open till'}
      </ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

export function OpenShiftScreen() {
  const branch = readSelectedBranch()
  const device = readSelectedDevice()
  const [floatAmount, setFloatAmount] = useState('100.00')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!readSelectedBranch() || !readSelectedDevice()) go('till')
  }, [])

  async function handleStart() {
    if (!branch || !device) return
    const openingFloat = Number(floatAmount)
    if (Number.isNaN(openingFloat) || openingFloat < 0) {
      setError('Enter a valid opening float')
      return
    }
    setBusy(true)
    setError(null)
    try {
      const shift = await openShift({
        branchId: branch.id,
        deviceId: device.id,
        openingFloat,
      })
      saveOpenShift(shift)
      go('opening')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not open shift')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthLayout>
      <BackButton route="till" label="Back to tills" />
      <div className="auth-status-icon"><AuthIcon name="cash" /></div>
      <h1 className="auth-title">Open your shift</h1>
      <p className="auth-subtitle">
        Count the opening float for {device?.name ?? 'your till'} at {branch?.name ?? 'your branch'}.
      </p>
      <div className="auth-notice auth-notice--info">
        <AuthIcon name="info" />
        <div>
          <strong>Cashier shift</strong>
          <span>Enter the cash already in the drawer before you start selling.</span>
        </div>
      </div>
      <label className="auth-label" htmlFor="opening-float">Opening float (USD)</label>
      <div className="auth-input">
        <span className="auth-input-plus">$</span>
        <input
          id="opening-float"
          inputMode="decimal"
          value={floatAmount}
          onChange={(event) => setFloatAmount(event.target.value)}
          placeholder="0.00"
        />
      </div>
      {error ? <p className="login-error" role="alert">{error}</p> : null}
      <ActionButton disabled={busy} onClick={() => void handleStart()}>
        {busy ? 'Opening…' : 'Start shift'}
      </ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

export function OpeningTillScreen() {
  const [step, setStep] = useState(0)

  useEffect(() => {
    const timers = [
      window.setTimeout(() => setStep(1), 700),
      window.setTimeout(() => setStep(2), 1400),
      window.setTimeout(() => go('dashboard-cashier'), 2200),
    ]
    return () => timers.forEach((timer) => window.clearTimeout(timer))
  }, [])

  return (
    <AuthLayout>
      <div className="auth-status-icon"><AuthIcon name="restore" /></div>
      <h1 className="auth-title">Opening your till</h1>
      <p className="auth-subtitle">Please wait while we prepare the cashier workspace.</p>
      <div className="auth-steps">
        <div className={step >= 0 ? 'done' : ''}><AuthIcon name="monitor" /><span>Till selected</span></div>
        <div className={step === 1 ? 'active' : step > 1 ? 'done' : ''}><AuthIcon name="cash" /><span>Opening cashier shift...</span></div>
        <div className={step >= 2 ? 'active' : ''}><AuthIcon name="cart" /><span>Loading checkout</span></div>
      </div>
      <SecureFooter />
    </AuthLayout>
  )
}

type DashboardRole = 'owner' | 'admin' | 'manager' | 'supervisor' | 'cashier' | 'stock'
type DashboardRange = 'Today' | '7 days' | '30 days'

type DashboardActivity = {
  time: string
  description: string
  detail: string
  amount: string
  status: string
}

type DashboardContent = {
  role: string
  title: string
  description: string
  metrics: { label: string; value: string; change: string; icon: IconName }[]
  priorities: { icon: IconName; title: string; detail: string; state: string }[]
  activities: DashboardActivity[]
}

const dashboardContent: Record<DashboardRole, DashboardContent> = {
  owner: {
    role: 'Owner',
    title: 'Business overview',
    description: 'A clear view of today across your stores.',
    metrics: [
      { label: 'Gross sales', value: '$48,230.45', change: '+8.4% vs last week', icon: 'chart' },
      { label: 'Transactions', value: '1,284', change: '+6.2% vs last week', icon: 'receipt' },
      { label: 'Gross margin', value: '22.8%', change: '+1.1% vs last week', icon: 'cash' },
      { label: 'Active branches', value: '6 of 6', change: 'All stores reporting', icon: 'monitor' },
    ],
    priorities: [
      { icon: 'alert', title: 'Fiscal device renewal', detail: 'One device certificate expires in 12 days.', state: 'Plan ahead' },
      { icon: 'chart', title: 'Best performing branch', detail: 'Avondale is 14% ahead of its daily target.', state: 'On track' },
      { icon: 'users', title: 'Team coverage', detail: '38 team members active across your stores.', state: 'On track' },
    ],
    activities: [
      { time: '14:32', description: 'Daily close completed', detail: 'Avondale · Till 03', amount: '$1,248.60', status: 'Complete' },
      { time: '14:26', description: 'Fiscal receipt submitted', detail: 'Harare CBD · Till 01', amount: 'Receipt #0842', status: 'Fiscalised' },
      { time: '14:18', description: 'Stock received', detail: 'Borrowdale · 12 line items', amount: 'GRN-0193', status: 'Received' },
      { time: '14:02', description: 'New team member added', detail: 'Mutare · Cashier', amount: 'T. Moyo', status: 'Complete' },
    ],
  },
  admin: {
    role: 'Administrator',
    title: 'Store administration',
    description: 'Your branches, people and devices at a glance.',
    metrics: [
      { label: 'Branches', value: '6', change: 'All branches active', icon: 'monitor' },
      { label: 'Team members', value: '38', change: '2 invitations pending', icon: 'users' },
      { label: 'Connected tills', value: '12 of 14', change: '2 need attention', icon: 'cash' },
      { label: 'Products', value: '1,248', change: '14 updated this week', icon: 'box' },
    ],
    priorities: [
      { icon: 'monitor', title: 'Tills need attention', detail: '2 devices have not synced recently.', state: 'Review' },
      { icon: 'mail', title: 'Pending invitations', detail: '2 team invitations are waiting for a response.', state: 'Pending' },
      { icon: 'receipt', title: 'Catalogue updates', detail: '14 product changes were made this week.', state: 'Up to date' },
    ],
    activities: [
      { time: '14:32', description: 'Till connected', detail: 'Avondale · Till 03', amount: 'Device online', status: 'Connected' },
      { time: '14:20', description: 'Product price updated', detail: 'Cooking oil · Harare CBD', amount: '$4.80', status: 'Updated' },
      { time: '13:54', description: 'Team invitation sent', detail: 'Mutare · Cashier', amount: '1 invitation', status: 'Pending' },
      { time: '13:41', description: 'Branch settings changed', detail: 'Borrowdale · Tax settings', amount: 'By S. Ncube', status: 'Complete' },
    ],
  },
  manager: {
    role: 'Manager',
    title: 'Operations overview',
    description: 'Sales, stock and day-to-day branch health.',
    metrics: [
      { label: 'Sales today', value: '$12,480.00', change: '+5.6% vs yesterday', icon: 'chart' },
      { label: 'Transactions', value: '342', change: '+18 since 12:00', icon: 'receipt' },
      { label: 'Low-stock items', value: '18', change: '6 need ordering soon', icon: 'box' },
      { label: 'Open shifts', value: '8', change: 'Across 4 tills', icon: 'clock' },
    ],
    priorities: [
      { icon: 'box', title: 'Low stock to review', detail: '6 products are below their reorder level.', state: 'Action needed' },
      { icon: 'restore', title: 'Offline sales to sync', detail: '3 transactions are waiting for a connection.', state: 'In queue' },
      { icon: 'cash', title: 'Shift reconciliation', detail: 'One shift is ready for its closing count.', state: 'Ready' },
    ],
    activities: [
      { time: '14:32', description: 'Sale completed', detail: 'Counter till 01 · Cashier', amount: '$86.40', status: 'Complete' },
      { time: '14:26', description: 'Stock adjustment submitted', detail: 'Soft drinks · +12 units', amount: 'Awaiting review', status: 'Pending' },
      { time: '14:18', description: 'Offline batch received', detail: 'Counter till 02 · 3 sales', amount: '$142.80', status: 'Synced' },
      { time: '14:02', description: 'Shift opened', detail: 'Counter till 04 · Cashier', amount: '$100.00 float', status: 'Open' },
    ],
  },
  supervisor: {
    role: 'Supervisor',
    title: 'Floor activity',
    description: 'Keep tills moving and approvals up to date.',
    metrics: [
      { label: 'Sales today', value: '$12,480.00', change: '+5.6% vs yesterday', icon: 'chart' },
      { label: 'Open tills', value: '8', change: 'All connected', icon: 'monitor' },
      { label: 'Approvals waiting', value: '4', change: 'Oldest is 8 minutes', icon: 'shield' },
      { label: 'Shifts to reconcile', value: '1', change: 'Ready to close', icon: 'cash' },
    ],
    priorities: [
      { icon: 'shield', title: 'Voids and returns', detail: '2 requests are waiting for your approval.', state: 'Review' },
      { icon: 'cash', title: 'Discount request', detail: 'Till 04 requested a 15% item discount.', state: '8 min ago' },
      { icon: 'receipt', title: 'Shift count ready', detail: 'Counter till 02 has submitted its closing count.', state: 'Ready' },
    ],
    activities: [
      { time: '14:32', description: 'Sale completed', detail: 'Counter till 01 · Cashier', amount: '$86.40', status: 'Complete' },
      { time: '14:26', description: 'Return requested', detail: 'Counter till 02 · Receipt #0837', amount: '$14.50', status: 'Awaiting' },
      { time: '14:18', description: 'Till reconnected', detail: 'Counter till 03', amount: 'Online', status: 'Resolved' },
      { time: '14:02', description: 'Discount requested', detail: 'Counter till 04 · 15% item discount', amount: 'Approval needed', status: 'Awaiting' },
    ],
  },
  cashier: {
    role: 'Cashier',
    title: 'My shift',
    description: 'Your Counter till 01 is open and ready for customers.',
    metrics: [
      { label: 'Sales this shift', value: '$1,248.60', change: 'Since 08:04', icon: 'chart' },
      { label: 'Transactions', value: '42', change: 'Last sale 2 min ago', icon: 'receipt' },
      { label: 'Average basket', value: '$29.73', change: 'Across today’s sales', icon: 'cart' },
      { label: 'Opening float', value: '$100.00', change: 'Cash drawer balanced', icon: 'cash' },
    ],
    priorities: [
      { icon: 'monitor', title: 'Counter till 01', detail: 'Connected · Harare CBD', state: 'Shift open' },
      { icon: 'restore', title: 'Offline ready', detail: 'Sales save on this device if the network drops.', state: 'Protected' },
      { icon: 'cash', title: 'Cash movements', detail: 'No paid-in or paid-out entries this shift.', state: 'All clear' },
    ],
    activities: [
      { time: '14:32', description: 'Sale completed', detail: '4 items · Cash', amount: '$86.40', status: 'Complete' },
      { time: '14:26', description: 'Sale completed', detail: '2 items · EcoCash', amount: '$14.50', status: 'Complete' },
      { time: '14:18', description: 'Sale completed', detail: '7 items · Card', amount: '$42.80', status: 'Complete' },
      { time: '14:02', description: 'Sale held', detail: '3 items · Waiting at till', amount: '$18.20', status: 'On hold' },
    ],
  },
  stock: {
    role: 'Stock controller',
    title: 'Inventory overview',
    description: 'What is moving, what is arriving and what needs a count.',
    metrics: [
      { label: 'Items below minimum', value: '18', change: '6 need ordering soon', icon: 'alert' },
      { label: 'Deliveries today', value: '3', change: '1 expected this afternoon', icon: 'truck' },
      { label: 'Stock accuracy', value: '98.4%', change: '+0.8% this month', icon: 'chart' },
      { label: 'Stock on hand', value: '$14,280', change: 'Across this branch', icon: 'box' },
    ],
    priorities: [
      { icon: 'alert', title: 'Reorder threshold reached', detail: '6 products are ready for a replenishment review.', state: 'Review' },
      { icon: 'truck', title: 'Delivery expected', detail: 'Fresh Foods Ltd · Estimated arrival 15:30.', state: 'Expected' },
      { icon: 'receipt', title: 'Cycle count due', detail: 'Aisle 3 · Beverages · 24 products.', state: 'Today' },
    ],
    activities: [
      { time: '14:32', description: 'Goods received', detail: 'Borrowdale · Fresh Foods Ltd', amount: '12 line items', status: 'Received' },
      { time: '14:10', description: 'Cycle count completed', detail: 'Aisle 2 · Household', amount: '32 products', status: 'Complete' },
      { time: '13:54', description: 'Wastage recorded', detail: 'Dairy · Expired stock', amount: '$18.40', status: 'Recorded' },
      { time: '13:41', description: 'Stock adjustment submitted', detail: 'Soft drinks · +12 units', amount: 'Awaiting review', status: 'Pending' },
    ],
  },
}

const dashboardSeries: Record<DashboardRange, number[]> = {
  Today: [18, 30, 25, 46, 38, 62, 54],
  '7 days': [28, 42, 35, 57, 48, 73, 66],
  '30 days': [32, 25, 44, 38, 59, 52, 76],
}

const dashboardLabels: Record<DashboardRange, string[]> = {
  Today: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00'],
  '7 days': ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
  '30 days': ['1', '5', '10', '15', '20', '25', '30'],
}

function DashboardScreen({ kind }: { kind: DashboardRole }) {
  const content = dashboardContent[kind]
  const user = readSessionUser()
  const displayName = user?.displayName?.trim() || 'there'
  const firstName = displayName.split(/\s+/)[0]
  const [range, setRange] = useState<DashboardRange>('Today')
  const [search, setSearch] = useState('')
  const activities = content.activities.filter((activity) =>
    `${activity.description} ${activity.detail} ${activity.amount} ${activity.status}`
      .toLowerCase()
      .includes(search.toLowerCase()),
  )
  const values = dashboardSeries[range]
  const points = values.map((value, index) => {
    const x = 52 + (index * 536) / (values.length - 1)
    const y = 174 - (value / Math.max(...values)) * 126
    return `${x},${y}`
  })

  return (
    <main className="shop-dashboard">
      <aside className="shop-dashboard__sidebar" aria-label="Workspace">
        <div className="shop-dashboard__brand">
          <span className="shop-dashboard__brand-mark" aria-hidden="true">
            <svg viewBox="0 0 24 24"><path d="M3 4h2l2.2 9.2a2 2 0 0 0 2 1.5h7.9a2 2 0 0 0 1.9-1.4L20.5 8H7" /><circle cx="9" cy="19" r="1.2" /><circle cx="17" cy="19" r="1.2" /></svg>
          </span>
          <span><strong>ShopOS</strong><small>RETAIL OPERATIONS</small></span>
        </div>
        <button className="shop-dashboard__branch" type="button" onClick={() => go('branch')}>
          <span className="shop-dashboard__branch-icon"><AuthIcon name="monitor" /></span>
          <span><small>YOUR BRANCH</small><strong>Harare CBD</strong></span>
          <span aria-hidden="true">⌄</span>
        </button>
        <div className="shop-dashboard__nav-label">WORKSPACE</div>
        <button className="shop-dashboard__nav-item" type="button" aria-current="page" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
          <AuthIcon name="chart" />
          <span>Overview</span>
          <span className="shop-dashboard__nav-dot" />
        </button>
        <div className="shop-dashboard__sidebar-note">
          <span className="shop-dashboard__online-dot" />
          <span><strong>All systems normal</strong><small>Last synced just now</small></span>
        </div>
        <div className="shop-dashboard__profile">
          <span className="shop-dashboard__avatar" aria-hidden="true">{firstName.slice(0, 1).toUpperCase()}</span>
          <span className="shop-dashboard__profile-copy"><strong>{displayName}</strong><small>{content.role}</small></span>
          <button type="button" aria-label="Log out" title="Log out" onClick={() => go('logout')}>
            <AuthIcon name="logout" />
          </button>
        </div>
      </aside>

      <div className="shop-dashboard__main">
        <header className="shop-dashboard__topbar">
          <div className="shop-dashboard__breadcrumbs"><span>Demo Grocers</span><span aria-hidden="true">/</span><strong>Overview</strong></div>
          <div className="shop-dashboard__topbar-meta">
            <span className="shop-dashboard__demo-label"><span /> Demo data</span>
            <button className="shop-dashboard__mobile-logout" type="button" onClick={() => go('logout')}>Log out</button>
          </div>
        </header>

        <div className="shop-dashboard__content">
          <section className="shop-dashboard__welcome" aria-labelledby="dashboard-title">
            <div>
              <p className="shop-dashboard__eyebrow">{content.role.toUpperCase()} WORKSPACE</p>
              <h1 id="dashboard-title">{content.title}</h1>
              <p><strong>Welcome back, {firstName}.</strong> {content.description}</p>
            </div>
            <button className="shop-dashboard__switch-button" type="button" aria-label="Switch branch" onClick={() => go('branch')}>
              <AuthIcon name="monitor" />
              <span>Switch branch</span>
            </button>
          </section>

          <section className="shop-dashboard__metrics" aria-label={`${content.role} summary`}>
            {content.metrics.map((metric, index) => (
              <article className="shop-dashboard__metric" key={metric.label}>
                <div className={`shop-dashboard__metric-icon shop-dashboard__metric-icon--${index + 1}`}>
                  <AuthIcon name={metric.icon} />
                </div>
                <p>{metric.label}</p>
                <strong>{metric.value}</strong>
                <small>{metric.change}</small>
              </article>
            ))}
          </section>

          <div className="shop-dashboard__grid">
            <section className="shop-dashboard__panel shop-dashboard__chart-panel" aria-labelledby="activity-chart-title">
              <div className="shop-dashboard__panel-heading">
                <div>
                  <h2 id="activity-chart-title">{kind === 'stock' ? 'Stock activity' : 'Sales activity'}</h2>
                  <p>{range === 'Today' ? 'A steady day across Harare CBD' : `Activity over the last ${range.toLowerCase()}`}</p>
                </div>
                <label className="shop-dashboard__range">
                  <span className="shop-dashboard__sr-only">Chart time period</span>
                  <select
                    value={range}
                    onChange={(event) => {
                      const selectedRange = event.target.value
                      if (selectedRange === 'Today' || selectedRange === '7 days' || selectedRange === '30 days') {
                        setRange(selectedRange)
                      }
                    }}
                  >
                    <option>Today</option>
                    <option>7 days</option>
                    <option>30 days</option>
                  </select>
                </label>
              </div>
              <div className="shop-dashboard__chart" role="img" aria-label={`${kind === 'stock' ? 'Stock' : 'Sales'} activity for ${range}`}>
                <svg viewBox="0 0 640 220" preserveAspectRatio="none" aria-hidden="true">
                  {[48, 90, 132, 174].map((y) => <line key={y} x1="52" x2="588" y1={y} y2={y} className="shop-dashboard__chart-grid" />)}
                  <path d={`M52,190 L${points.join(' L')} L588,190 Z`} className="shop-dashboard__chart-area" />
                  <polyline points={points.join(' ')} className="shop-dashboard__chart-line" />
                  {points.map((point) => {
                    const [cx, cy] = point.split(',')
                    return <circle key={point} cx={cx} cy={cy} r="4" className="shop-dashboard__chart-point" />
                  })}
                  {dashboardLabels[range].map((label, index) => (
                    <text key={label} x={52 + (index * 536) / 6} y="214" textAnchor="middle" className="shop-dashboard__chart-label">{label}</text>
                  ))}
                </svg>
              </div>
              <div className="shop-dashboard__chart-foot"><span><i /> Activity trend</span><span>Sample data · {range}</span></div>
            </section>

            <section className="shop-dashboard__panel shop-dashboard__priorities" aria-labelledby="priorities-title">
              <div className="shop-dashboard__panel-heading">
                <div><h2 id="priorities-title">Worth a look</h2><p>Updates from your workspace</p></div>
                <span className="shop-dashboard__count">{content.priorities.length}</span>
              </div>
              <div className="shop-dashboard__priority-list">
                {content.priorities.map((item) => (
                  <article className="shop-dashboard__priority" key={item.title}>
                    <span className="shop-dashboard__priority-icon"><AuthIcon name={item.icon} /></span>
                    <span className="shop-dashboard__priority-copy"><strong>{item.title}</strong><small>{item.detail}</small><em>{item.state}</em></span>
                  </article>
                ))}
              </div>
            </section>
          </div>

          <section className="shop-dashboard__panel shop-dashboard__activity-panel" aria-labelledby="recent-activity-title">
            <div className="shop-dashboard__panel-heading shop-dashboard__activity-heading">
              <div><h2 id="recent-activity-title">Recent activity</h2><p>The latest updates from your branch</p></div>
              <label className="shop-dashboard__search">
                <AuthIcon name="chart" />
                <input aria-label="Search recent activity" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search activity" />
              </label>
            </div>
            <div className="shop-dashboard__table-wrap">
              <table className="shop-dashboard__table">
                <thead><tr><th scope="col">Activity</th><th scope="col">Details</th><th scope="col">Reference</th><th scope="col">Status</th><th scope="col">Time</th></tr></thead>
                <tbody>
                  {activities.map((activity) => (
                    <tr key={`${activity.time}-${activity.description}`}>
                      <td><strong>{activity.description}</strong></td>
                      <td>{activity.detail}</td>
                      <td>{activity.amount}</td>
                      <td><span className="shop-dashboard__status">{activity.status}</span></td>
                      <td>{activity.time}</td>
                    </tr>
                  ))}
                  {activities.length === 0 && <tr><td className="shop-dashboard__empty" colSpan={5}>No activity matches “{search}”.</td></tr>}
                </tbody>
              </table>
            </div>
          </section>
          <footer className="shop-dashboard__footer">ShopOS · Demo workspace <span>•</span> Harare CBD</footer>
        </div>
      </div>
    </main>
  )
}

export function CashierDashboardScreen() {
  return <DashboardScreen kind="cashier" />
}

export function OwnerDashboardScreen() {
  return <DashboardScreen kind="owner" />
}

export function AdminDashboardScreen() {
  return <DashboardScreen kind="admin" />
}

export function ManagerDashboardScreen() {
  return <DashboardScreen kind="manager" />
}

export function SupervisorDashboardScreen() {
  return <DashboardScreen kind="supervisor" />
}

export function StockDashboardScreen() {
  return <DashboardScreen kind="stock" />
}

export function RestoringSessionScreen() {
  const [step, setStep] = useState(0)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        setStep(1)
        await refreshSession()
        if (cancelled) return
        setStep(2)
        const user = await me()
        if (cancelled) return
        const role = primaryRole(user)
        saveSession(user, role)
        go(dashboardRouteForRole(role))
      } catch (err) {
        if (cancelled) return
        setError(err instanceof ApiError ? err.message : 'Session could not be restored')
        clearSession()
        go('expired')
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <AuthLayout>
      <div className="auth-status-icon"><AuthIcon name="restore" /></div>
      <h1 className="auth-title">Restoring your session</h1>
      <p className="auth-subtitle">Please wait while we refresh your access.</p>
      <div className="auth-steps">
        <div className="done"><AuthIcon name="shield" /><span>Access token expired</span></div>
        <div className={step === 1 ? 'active' : step > 1 ? 'done' : ''}>
          <AuthIcon name="restore" /><span>Requesting a new access token...</span>
        </div>
        <div className={step >= 2 ? 'active' : ''}><AuthIcon name="lock" /><span>Restoring your session</span></div>
      </div>
      {error ? <p className="login-error" role="alert">{error}</p> : null}
      <ActionButton secondary onClick={() => go('login')}>Cancel and sign in</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
