import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, BackButton, Notice, SecureFooter, StatusIcon, Subtitle, Title } from '@/components/auth/ui'

export default function AccountDisabledScreen() {
  const { replace } = useAuthNav()
  return (
    <AuthLayout>
      <BackButton onPress={() => replace('login')} />
      <StatusIcon name="block" />
      <Title>Account disabled</Title>
      <Subtitle>Your account has been disabled and can't be used to sign in right now.</Subtitle>
      <Notice>
        Access to ShopOS is restricted for this account. Contact your administrator to have it re-enabled.
      </Notice>
      <ActionButton>Contact administrator</ActionButton>
      <ActionButton secondary onPress={() => replace('login')}>Back to sign in</ActionButton>
      <ActionButton secondary>Open till anyway</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
