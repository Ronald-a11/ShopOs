import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, SecureFooter, StatusIcon, Steps, Subtitle, Title } from '@/components/auth/ui'

export default function RestoringSessionScreen() {
  const { replace } = useAuthNav()
  return (
    <AuthLayout>
      <StatusIcon name="restore" />
      <Title>Restoring your session</Title>
      <Subtitle>Please wait while we refresh your access.</Subtitle>
      <Steps
        steps={[
          { icon: 'shield', label: 'Access token expired', state: 'done' },
          { icon: 'restore', label: 'Requesting a new access token...', state: 'active' },
          { icon: 'lock', label: 'Restoring your session' },
        ]}
      />
      <ActionButton secondary onPress={() => replace('login')}>Cancel and sign in</ActionButton>
      <ActionButton secondary>Skip to POS</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
