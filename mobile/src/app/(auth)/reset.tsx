import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import {
  ActionButton,
  BackButton,
  Label,
  Requirements,
  SecureFooter,
  Subtitle,
  TextField,
  Title,
} from '@/components/auth/ui'

export default function ResetPasswordScreen() {
  const { replace } = useAuthNav()
  return (
    <AuthLayout>
      <BackButton onPress={() => replace('login')} />
      <Title>Reset Password</Title>
      <Subtitle>Create a new password for your ShopOS account.</Subtitle>
      <Label>New password</Label>
      <TextField icon="lock" placeholder="Enter new password" secure autoComplete="new-password" />
      <Requirements
        items={[
          'At least 8 characters',
          'One uppercase letter',
          'One number',
          'One special character (e.g. !@#$%)',
        ]}
      />
      <Label>Confirm new password</Label>
      <TextField icon="lock" placeholder="Confirm new password" secure autoComplete="new-password" />
      <ActionButton onPress={() => replace('login')}>Reset password</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
