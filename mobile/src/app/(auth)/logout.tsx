import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, BackButton, Notice, SecureFooter, StatusIcon, Subtitle, Title } from '@/components/auth/ui'

export default function LogoutScreen() {
  const { replace } = useAuthNav()
  return (
    <AuthLayout>
      <BackButton label="Back to till" onPress={() => replace('till')} />
      <StatusIcon name="logout" />
      <Title>Log out of ShopOS?</Title>
      <Subtitle>You will need to sign in again to access the till.</Subtitle>
      <Notice>Finish or hold any open sale before logging out so nothing is left pending.</Notice>
      <ActionButton onPress={() => replace('login')}>Log out</ActionButton>
      <ActionButton secondary onPress={() => replace('till')}>Stay signed in</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
