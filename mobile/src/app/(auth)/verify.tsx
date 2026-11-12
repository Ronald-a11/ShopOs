import { useState } from 'react'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import {
  ActionButton,
  BackButton,
  Divider,
  Label,
  Notice,
  SecureFooter,
  Segmented,
  Subtitle,
  TextField,
  TextLink,
  Title,
} from '@/components/auth/ui'

export default function AccountVerificationScreen() {
  const { replace } = useAuthNav()
  const [method, setMethod] = useState<'email' | 'phone'>('email')
  return (
    <AuthLayout>
      <BackButton onPress={() => replace('login')} />
      <Title>Account Verification</Title>
      <Subtitle>Verify/activate your account, if required.</Subtitle>
      <Notice icon="shield" title="We're almost there!" info>
        To keep your account secure, please verify your email address or phone number.
      </Notice>
      <Label>Verification method</Label>
      <Segmented
        value={method}
        onChange={setMethod}
        options={[
          { value: 'email', label: 'Email verification' },
          { value: 'phone', label: 'Phone verification' },
        ]}
      />
      <Label>{method === 'email' ? 'Email address' : 'Phone number'}</Label>
      {method === 'email' ? (
        <TextField key="email" icon="mail" placeholder="name@yourstore.co.zw" keyboardType="email-address" />
      ) : (
        <TextField key="phone" prefix="+" placeholder="263 00 000 0000" keyboardType="phone-pad" />
      )}
      <ActionButton>Send verification code</ActionButton>
      <Divider />
      <TextLink>I already have a verification code</TextLink>
      <SecureFooter />
    </AuthLayout>
  )
}
