import { useRouter } from 'expo-router'

export type AuthRoute =
  | 'login'
  | 'forgot'
  | 'reset'
  | 'verify'
  | 'expired'
  | 'logout'
  | 'denied'
  | 'disabled'
  | 'branch'
  | 'till'
  | 'restoring'

// Same order as authPreviewRoutes in frontend/src/auth/AuthScreens.tsx.
export const previewOrder: AuthRoute[] = [
  'forgot',
  'reset',
  'verify',
  'expired',
  'logout',
  'denied',
  'disabled',
  'branch',
  'till',
  'restoring',
  'login',
]

export function useAuthNav() {
  const router = useRouter()
  return {
    /** Move forward to a screen, keeping history so the system back gesture works. */
    go: (route: AuthRoute) => router.push(`/${route}`),
    /** "Back to …" buttons go to a fixed screen, like the web hash links. */
    replace: (route: AuthRoute) => router.replace(`/${route}`),
  }
}
