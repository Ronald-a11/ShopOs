import { useState, type FormEvent, type ReactNode } from 'react'
import { login, AuthApiError } from '../api/auth'
import {
  DEMO_ACCOUNTS,
  entryRouteForRole,
  primaryRole,
  saveSession,
  type DemoAccount,
} from './session'
import './LoginScreen.css'

function go(route: string) {
  window.location.hash = route
}

function Svg({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true">
      {children}
    </svg>
  )
}

function BrandMark() {
  return (
    <span className="login-brand-mark">
      <svg viewBox="0 0 24 24" role="img" aria-label="ShopOS">
        <path d="M3 4h2l2.2 9.2a2 2 0 0 0 2 1.5h7.9a2 2 0 0 0 1.9-1.4L20.5 8H7" />
        <circle cx="9" cy="19" r="1.2" />
        <circle cx="17" cy="19" r="1.2" />
      </svg>
    </span>
  )
}

/** Brand row shown above the form when the story panel is hidden on narrow screens. */
export function CompactBrand() {
  return (
    <div className="login-compact-brand">
      <BrandMark />
      <strong>ShopOS</strong>
      <span className="login-brand-pill">POS</span>
    </div>
  )
}

const storyFeatures: { label: string; icon: ReactNode }[] = [
  {
    label: 'Fast checkouts',
    icon: <><path d="M3 4h2l2.2 9.2a2 2 0 0 0 2 1.5h7.9a2 2 0 0 0 1.9-1.4L20.5 8H7" /><circle cx="9" cy="19" r="1.2" /><circle cx="17" cy="19" r="1.2" /></>,
  },
  {
    label: 'Real-time inventory',
    icon: <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Zm0 9 8-4.5M12 12 4 7.5M12 12v9" />,
  },
  {
    label: 'Secure & reliable',
    icon: <path d="M12 3 20 6v5c0 5-3.4 8.4-8 10-4.6-1.6-8-5-8-10V6l8-3Zm-3 9 2 2 4-4" />,
  },
  {
    label: 'Multi-branch ready',
    icon: <path d="M4 10v10h16V10M3 10l2-6h14l2 6H3Zm6 10v-5h6v5" />,
  },
]

export function LoginStoryPanel({
  animateBrand = false,
  animateLogo = false,
}: {
  animateBrand?: boolean
  animateLogo?: boolean
}) {
  const brandClass = [
    'login-story__brand',
    animateBrand ? 'login-story__brand--animate' : '',
    animateLogo ? 'login-story__brand--logo-animate' : '',
  ].filter(Boolean).join(' ')

  return (
    <section className="login-story" aria-label="ShopOS introduction">
      <img className="login-story__photo" src="/pos-terminal.png" alt="" />
      <div className="login-story__rings" aria-hidden="true">
        <span />
        <span />
      </div>

      <div className={brandClass}>
        <BrandMark />
        <div>
          <strong>ShopOS</strong>
          <span className="login-brand-pill">POS</span>
        </div>
      </div>

      <div className="login-story__copy">
        <span className="login-story__eyebrow">BUILT FOR MODERN RETAIL</span>
        <h1>
          Smarter checkouts.
          <br />
          Better inventory.
          <br />
          A <em>stronger</em> business.
        </h1>
        <p>
          ShopOS helps you manage your store, track your stock and serve your
          customers, all in one place.
        </p>
        <ul className="login-story__features">
          {storyFeatures.map(({ label, icon }) => (
            <li key={label}>
              <Svg>{icon}</Svg>
              <span>{label}</span>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}

export function SecureNote() {
  return (
    <p className="login-secure-note">
      <Svg>
        <rect x="6" y="10" width="12" height="10" rx="2" />
        <path d="M9 10V7a3 3 0 0 1 6 0v3M12 14v2" />
      </Svg>
      <span>Your data is secure and encrypted</span>
    </p>
  )
}

async function signInAs(email: string, password: string, rememberMe: boolean) {
  const user = await login(email, password, rememberMe)
  const role = primaryRole(user)
  saveSession(user, role)
  go(entryRouteForRole(role))
}

function LoginScreen() {
  const [showPassword, setShowPassword] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await signInAs(email, password, rememberMe)
    } catch (err) {
      if (err instanceof AuthApiError && err.code === 'ACCOUNT_DISABLED') {
        go('disabled')
        return
      }
      if (err instanceof AuthApiError && err.code === 'ACCOUNT_UNVERIFIED') {
        go('verify')
        return
      }
      setError(err instanceof AuthApiError ? err.message : 'Sign in failed')
    } finally {
      setBusy(false)
    }
  }

  async function handleDemo(account: DemoAccount) {
    setEmail(account.email)
    setPassword(account.password)
    setBusy(true)
    setError(null)
    try {
      await signInAs(account.email, account.password, false)
    } catch (err) {
      if (err instanceof AuthApiError && err.code === 'ACCOUNT_DISABLED') {
        go('disabled')
        return
      }
      if (err instanceof AuthApiError && err.code === 'ACCOUNT_UNVERIFIED') {
        go('verify')
        return
      }
      setError(err instanceof AuthApiError ? err.message : 'Sign in failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="login-screen">
      <LoginStoryPanel animateBrand />

      <section className="login-panel-wrap" aria-label="Sign in">
        <CompactBrand />

        <div className="login-panel">
          <header className="login-panel__header">
            <h2>Welcome back</h2>
            <p>Sign in to continue managing your store.</p>
          </header>

          <form className="login-form" onSubmit={handleSubmit}>
            <div className="login-field">
              <label htmlFor="email">Email address</label>
              <div className="login-input-wrap">
                <Svg className="login-input-icon">
                  <rect x="3" y="5" width="18" height="14" rx="2" />
                  <path d="m4 7 8 6 8-6" />
                </Svg>
                <input
                  id="email"
                  name="email"
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="name@yourstore.co.zw"
                  autoComplete="email"
                  required
                />
              </div>
            </div>

            <div className="login-field">
              <div className="login-label-row">
                <label htmlFor="password">Password</label>
                <button className="login-text-button" type="button" onClick={() => go('forgot')}>
                  Forgot password?
                </button>
              </div>
              <div className="login-input-wrap">
                <Svg className="login-input-icon">
                  <rect x="5" y="10" width="14" height="11" rx="2" />
                  <path d="M8 10V7a4 4 0 0 1 8 0v3" />
                </Svg>
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  required
                />
                <button
                  className="login-password-toggle"
                  type="button"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  aria-pressed={showPassword}
                  onClick={() => setShowPassword((visible) => !visible)}
                >
                  {showPassword ? (
                    <Svg>
                      <path d="M3 3l18 18M10.6 6.1A10.7 10.7 0 0 1 12 6c6.5 0 10 6 10 6a17.6 17.6 0 0 1-3.1 3.8M6.6 6.6C3.8 8.4 2 12 2 12s3.5 6 10 6a10 10 0 0 0 5.4-1.6M9.9 9.9a3 3 0 0 0 4.2 4.2" />
                    </Svg>
                  ) : (
                    <Svg>
                      <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" />
                      <circle cx="12" cy="12" r="2.5" />
                    </Svg>
                  )}
                </button>
              </div>
            </div>

            <label className="login-remember">
              <input
                type="checkbox"
                name="remember"
                checked={rememberMe}
                onChange={(event) => setRememberMe(event.target.checked)}
              />
              <span>Keep me signed in on this device</span>
            </label>

            {error ? <p className="login-error" role="alert">{error}</p> : null}

            <button className="login-submit" type="submit" disabled={busy}>
              <span>{busy ? 'Signing in…' : 'Sign in'}</span>
              <Svg>
                <path d="M5 12h13M13 7l5 5-5 5" />
              </Svg>
            </button>
          </form>

          <div className="login-demo" aria-label="Demo accounts from technical documentation roles">
            <p className="login-demo__title">Demo users (shared password <code>Password1!</code>)</p>
            <div className="login-demo__list">
              {DEMO_ACCOUNTS.map((account) => (
                <button
                  key={account.email}
                  type="button"
                  className="login-demo__item"
                  disabled={busy}
                  onClick={() => void handleDemo(account)}
                >
                  <strong>{account.label}</strong>
                  <span>{account.email}</span>
                  <small>{account.summary}</small>
                </button>
              ))}
            </div>
          </div>
        </div>

        <SecureNote />
      </section>
    </main>
  )
}

export default LoginScreen
