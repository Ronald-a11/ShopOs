import { useEffect, useState, type ReactNode } from 'react'
import LoginScreen from './auth/LoginScreen'
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
  RestoringSessionScreen,
  ResetPasswordScreen,
  SelectBranchScreen,
  SelectTillScreen,
  SessionExpiredScreen,
  StockDashboardScreen,
  SupervisorDashboardScreen,
} from './auth/AuthScreens'

function App() {
  const [route, setRoute] = useState(() => window.location.hash.slice(1) || 'login')

  useEffect(() => {
    const handleHashChange = () => {
      const nextRoute = window.location.hash.slice(1) || 'login'
      const updateRoute = () => setRoute(nextRoute)

      if ('startViewTransition' in document) {
        document.startViewTransition(updateRoute)
      } else {
        updateRoute()
      }
    }
    window.addEventListener('hashchange', handleHashChange)
    return () => window.removeEventListener('hashchange', handleHashChange)
  }, [])

  const screens: Record<string, ReactNode> = {
    login: <LoginScreen />,
    forgot: <ForgotPasswordScreen />,
    reset: <ResetPasswordScreen />,
    verify: <AccountVerificationScreen />,
    expired: <SessionExpiredScreen />,
    logout: <LogoutScreen />,
    denied: <AccessDeniedScreen />,
    disabled: <AccountDisabledScreen />,
    branch: <SelectBranchScreen />,
    till: <SelectTillScreen />,
    shift: <OpenShiftScreen />,
    opening: <OpeningTillScreen />,
    dashboard: <CashierDashboardScreen />,
    'dashboard-cashier': <CashierDashboardScreen />,
    'dashboard-owner': <OwnerDashboardScreen />,
    'dashboard-admin': <AdminDashboardScreen />,
    'dashboard-manager': <ManagerDashboardScreen />,
    'dashboard-supervisor': <SupervisorDashboardScreen />,
    'dashboard-stock': <StockDashboardScreen />,
    restoring: <RestoringSessionScreen />,
  }

  return screens[route] ?? <LoginScreen />
}

export default App
