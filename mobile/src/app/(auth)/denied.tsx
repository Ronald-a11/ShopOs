import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, BackButton, Notice, SecureFooter, StatusIcon, Subtitle, Title } from '@/components/auth/ui'

export default function AccessDeniedScreen() {
  const { replace } = useAuthNav()
  return (
    <AuthLayout>
      <BackButton label="Back to till" onPress={() => replace('till')} />
      <StatusIcon name="shield" />
      <Title>Access denied</Title>
      <Subtitle>This role would normally be blocked. In the mock-up you can continue anyway.</Subtitle>
      <Notice>Requested page: Reports. Required role: Manager.</Notice>
      <ActionButton>Open reports anyway</ActionButton>
      <ActionButton secondary>Request access</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
