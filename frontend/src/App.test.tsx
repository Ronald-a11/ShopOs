import { fireEvent, render, screen } from '@testing-library/react'
import App from './App'
import {
  AccountDisabledScreen,
  AccountVerificationScreen,
  AccessDeniedScreen,
  AdminDashboardScreen,
  CashierDashboardScreen,
  ForgotPasswordScreen,
  LogoutScreen,
  ManagerDashboardScreen,
  OpeningTillScreen,
  OpenShiftScreen,
  OwnerDashboardScreen,
  ResetPasswordScreen,
  RestoringSessionScreen,
  SelectBranchScreen,
  SelectTillScreen,
  SessionExpiredScreen,
  StockDashboardScreen,
  SupervisorDashboardScreen,
} from './auth/AuthScreens'

test('renders the login screen with demo credentials', () => {
  render(<App />)
  expect(screen.getByRole('heading', { name: /Welcome back/i })).toBeInTheDocument()
  expect(screen.getByLabelText('Email address')).toBeInTheDocument()
  expect(screen.getByLabelText('Password')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument()
  expect(screen.getByText('owner@demo.shopos.co.zw')).toBeInTheDocument()
  expect(screen.getByText('cashier@demo.shopos.co.zw')).toBeInTheDocument()
  expect(screen.queryByRole('button', { name: 'Next' })).not.toBeInTheDocument()
})

test.each([
  ['forgot password', ForgotPasswordScreen],
  ['reset password', ResetPasswordScreen],
  ['account verification', AccountVerificationScreen],
  ['session expired', SessionExpiredScreen],
  ['logout', LogoutScreen],
  ['access denied', AccessDeniedScreen],
  ['disabled account', AccountDisabledScreen],
  ['branch selection', SelectBranchScreen],
  ['till selection', SelectTillScreen],
  ['open shift', OpenShiftScreen],
  ['opening till', OpeningTillScreen],
  ['cashier dashboard', CashierDashboardScreen],
  ['owner dashboard', OwnerDashboardScreen],
  ['admin dashboard', AdminDashboardScreen],
  ['manager dashboard', ManagerDashboardScreen],
  ['supervisor dashboard', SupervisorDashboardScreen],
  ['stock dashboard', StockDashboardScreen],
  ['session restoration', RestoringSessionScreen],
])('does not render a Next button on the %s screen', (_name, Screen) => {
  render(<Screen />)
  expect(screen.queryByRole('button', { name: 'Next' })).not.toBeInTheDocument()
})

test.each([
  ['owner', OwnerDashboardScreen, '$48,230.45'],
  ['admin', AdminDashboardScreen, '2 invitations pending'],
  ['manager', ManagerDashboardScreen, 'Low-stock items'],
  ['supervisor', SupervisorDashboardScreen, 'Approvals waiting'],
  ['cashier', CashierDashboardScreen, 'My shift'],
  ['stock controller', StockDashboardScreen, 'Stock accuracy'],
])('shows a role-specific dashboard for %s', (_role, Screen, expected) => {
  render(<Screen />)
  expect(screen.getByText(expected)).toBeInTheDocument()
  expect(screen.getByText('Demo data')).toBeInTheDocument()
})

test('filters dashboard activity and updates its chart period', () => {
  render(<CashierDashboardScreen />)

  fireEvent.change(screen.getByRole('textbox', { name: 'Search recent activity' }), {
    target: { value: 'EcoCash' },
  })
  expect(screen.getByText('2 items · EcoCash')).toBeInTheDocument()
  expect(screen.queryByText('4 items · Cash')).not.toBeInTheDocument()

  fireEvent.change(screen.getByRole('combobox', { name: 'Chart time period' }), {
    target: { value: '7 days' },
  })
  expect(screen.getByRole('img', { name: 'Sales activity for 7 days' })).toBeInTheDocument()
})
