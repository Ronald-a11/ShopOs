import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, Divider, Label, SecureFooter, Subtitle, TextField, Title } from '@/components/auth/ui'

export default function ForgotPasswordScreen() {
  const { go, replace } = useAuthNav()
  return (
    <AuthLayout>
      <Title>Forgot password?</Title>
      <Subtitle>Enter your email address and we'll send you a link to reset your password.</Subtitle>
      <Label>Email address</Label>
      <TextField icon="mail" placeholder="name@yourstore.co.zw" keyboardType="email-address" autoComplete="email" />
      <ActionButton onPress={() => go('reset')}>Send recovery link</ActionButton>
      <Divider />
      <ActionButton secondary onPress={() => replace('login')}>Back to sign in</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
