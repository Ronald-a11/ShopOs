import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, Notice, SecureFooter, StatusIcon, Subtitle, Title } from '@/components/auth/ui'

export default function SessionExpiredScreen() {
  const { go, replace } = useAuthNav()
  return (
    <AuthLayout>
      <StatusIcon name="clock" />
      <Title>Session expired</Title>
      <Subtitle>Your session has timed out. Please authenticate again to continue.</Subtitle>
      <Notice>Your account is safe. No changes were made while you were signed out.</Notice>
      <ActionButton onPress={() => replace('login')}>Sign in again</ActionButton>
      <ActionButton secondary onPress={() => go('till')}>Open till</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
